package com.example.kuwalog.service;

import com.example.kuwalog.dto.BeetleForm;
import com.example.kuwalog.entity.Beetle;
import com.example.kuwalog.entity.User;
import com.example.kuwalog.repository.BeetleRepository;
import com.example.kuwalog.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class BeetleService {

    private final BeetleRepository beetleRepository;
    private final UserRepository userRepository;

    public BeetleService(BeetleRepository beetleRepository, UserRepository userRepository) {
        this.beetleRepository = beetleRepository;
        this.userRepository = userRepository;
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
        beetle.setName(form.getName());
        beetle.setSex(form.getSex().getLabel());
        beetle.setStage(form.getStage().getLabel());
        beetle.setGeneration(form.getGeneration());
        beetle.setLocality(form.getLocality());
        beetle.setEmergenceDate(form.getEmergenceDate());
        beetle.setDescription(form.getDescription());
        return beetleRepository.save(beetle);
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

        beetle.setName(form.getName());
        beetle.setSex(form.getSex().getLabel());
        beetle.setStage(form.getStage().getLabel());
        beetle.setGeneration(form.getGeneration());
        beetle.setLocality(form.getLocality());
        beetle.setEmergenceDate(form.getEmergenceDate());
        beetle.setDescription(form.getDescription());
        return beetle;
    }

    @Transactional
    public void delete(Long id, String username) {
        Beetle beetle = beetleRepository.findByIdWithUser(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "生体が見つかりません"));

        if (!beetle.getUser().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "削除権限がありません");
        }

        beetleRepository.delete(beetle);
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません: " + username));
    }
}
