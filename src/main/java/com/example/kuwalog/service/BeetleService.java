package com.example.kuwalog.service;

import com.example.kuwalog.dto.BeetleForm;
import com.example.kuwalog.entity.Beetle;
import com.example.kuwalog.entity.User;
import com.example.kuwalog.entity.enums.Classification;
import com.example.kuwalog.entity.enums.Sex;
import com.example.kuwalog.entity.enums.Stage;
import com.example.kuwalog.entity.UsedBeetlePublicId;
import com.example.kuwalog.repository.BeetleRepository;
import com.example.kuwalog.repository.ReviewRepository;
import com.example.kuwalog.repository.TransactionRepository;
import com.example.kuwalog.repository.UsedBeetlePublicIdRepository;
import com.example.kuwalog.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class BeetleService {

    private final BeetleRepository beetleRepository;
    private final UserRepository userRepository;
    private final UsedBeetlePublicIdRepository usedPublicIdRepository;
    private final TransactionRepository transactionRepository;
    private final ReviewRepository reviewRepository;

    public BeetleService(BeetleRepository beetleRepository, UserRepository userRepository,
                         UsedBeetlePublicIdRepository usedPublicIdRepository,
                         TransactionRepository transactionRepository,
                         ReviewRepository reviewRepository) {
        this.beetleRepository = beetleRepository;
        this.userRepository = userRepository;
        this.usedPublicIdRepository = usedPublicIdRepository;
        this.transactionRepository = transactionRepository;
        this.reviewRepository = reviewRepository;
    }

    @Transactional(readOnly = true)
    public List<Beetle> findWithFilters(Classification classification, Sex sex, Stage stage, String locality) {
        String cls = classification != null ? classification.getLabel() : null;
        String s   = sex != null ? sex.getLabel() : null;
        String st  = stage != null ? stage.getLabel() : null;
        String loc = (locality != null && !locality.isBlank()) ? locality.trim() : null;

        return beetleRepository.findAllWithUser().stream()
                .filter(b -> cls == null || cls.equals(b.getClassification()))
                .filter(b -> s   == null || s.equals(b.getSex()))
                .filter(b -> st  == null || st.equals(b.getStage()))
                .filter(b -> loc == null || (b.getLocality() != null && b.getLocality().contains(loc)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Beetle> findByUsername(String username) {
        User user = getUser(username);
        return beetleRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional
    public Beetle post(BeetleForm form, String username) {
        User user = getUser(username);

        Beetle beetle = new Beetle();
        beetle.setUser(user);
        applyForm(beetle, form);
        beetle.setPublicId(generatePublicId(form.getClassification()));
        return beetleRepository.save(beetle);
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    @Transactional(readOnly = true)
    public List<Beetle> findParentCandidates(Sex sex, String username) {
        User user = getUser(username);
        return beetleRepository.findParentCandidates(sex.getLabel(), Stage.ADULT.getLabel(), user);
    }

    @Transactional(readOnly = true)
    public Beetle findById(Long id) {
        return beetleRepository.findByIdWithUser(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "生体が見つかりません"));
    }

    @Transactional
    public Beetle update(Long id, BeetleForm form, String username) {
        Beetle beetle = beetleRepository.findByIdWithUser(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "生体が見つかりません"));

        if (!beetle.getUser().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "編集権限がありません");
        }

        applyForm(beetle, form);
        return beetle;
    }

    @Transactional
    public void delete(Long id, String username) {
        Beetle beetle = beetleRepository.findByIdWithUser(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "生体が見つかりません"));

        if (!beetle.getUser().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "削除権限がありません");
        }

        // 紐づく譲渡記録とレビューを先に削除（FK制約のため）
        transactionRepository.findByBeetleWithUsers(beetle).forEach(t -> {
            reviewRepository.deleteByTransaction(t);
            transactionRepository.delete(t);
        });

        if (beetle.getPublicId() != null) {
            usedPublicIdRepository.save(new UsedBeetlePublicId(beetle.getPublicId()));
        }
        beetleRepository.delete(beetle);
    }

    // post/updateで共通のフォーム→エンティティ反映処理。父母バリデーションもここで行う
    private void applyForm(Beetle beetle, BeetleForm form) {
        beetle.setName(form.getName());
        beetle.setClassification(form.getClassification() != null ? form.getClassification().getLabel() : null);
        beetle.setSex(form.getSex().getLabel());
        beetle.setStage(form.getStage().getLabel());
        beetle.setGeneration(form.getGeneration());
        beetle.setLocality(form.getLocality());
        beetle.setEmergenceDate(form.getEmergenceDate());
        beetle.setDescription(form.getDescription());
        beetle.setBreederName(form.getBreederName());
        beetle.setSizeMm(form.getSizeMm());
        beetle.setWeightG(form.getWeightG());

        if (form.getFatherId() != null) {
            Beetle father = beetleRepository.findByIdWithUser(form.getFatherId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "父個体が見つかりません"));
            if (beetle.getId() != null && beetle.getId().equals(father.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "自分自身を父個体に設定できません");
            }
            if (!Sex.MALE.getLabel().equals(father.getSex())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "父個体にはオスの生体のみ選択できます");
            }
            beetle.setFather(father);
        } else {
            beetle.setFather(null);
        }

        if (form.getMotherId() != null) {
            Beetle mother = beetleRepository.findByIdWithUser(form.getMotherId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "母個体が見つかりません"));
            if (beetle.getId() != null && beetle.getId().equals(mother.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "自分自身を母個体に設定できません");
            }
            if (!Sex.FEMALE.getLabel().equals(mother.getSex())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "母個体にはメスの生体のみ選択できます");
            }
            beetle.setMother(mother);
        } else {
            beetle.setMother(null);
        }
    }

    private String generatePublicId(Classification classification) {
        int year = LocalDate.now().getYear() % 100;
        String prefix = (classification == Classification.KABUTOMUSHI ? "B" : "S")
                + String.format("%02d", year);

        Integer maxInBeetles = beetleRepository.findMaxSequenceByPublicIdPrefix(prefix);
        Integer maxInUsed = usedPublicIdRepository.findMaxSequenceByPrefix(prefix);

        int next = 1;
        if (maxInBeetles != null) next = Math.max(next, maxInBeetles + 1);
        if (maxInUsed != null) next = Math.max(next, maxInUsed + 1);

        return prefix + String.format("%06d", next);
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません: " + username));
    }
}
