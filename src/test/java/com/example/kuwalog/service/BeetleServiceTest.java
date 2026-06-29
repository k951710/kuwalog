package com.example.kuwalog.service;

import com.example.kuwalog.dto.BeetleForm;
import com.example.kuwalog.entity.Beetle;
import com.example.kuwalog.entity.User;
import com.example.kuwalog.entity.enums.Sex;
import com.example.kuwalog.entity.enums.Stage;
import com.example.kuwalog.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BeetleServiceTest {

    @Mock private BeetleRepository beetleRepository;
    @Mock private UserRepository userRepository;
    @Mock private UsedBeetlePublicIdRepository usedPublicIdRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private BeetleImageService beetleImageService;

    @InjectMocks
    private BeetleService beetleService;

    private User owner;
    private User other;
    private Beetle beetle;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setUsername("owner");

        other = new User();
        other.setId(2L);
        other.setUsername("other");

        beetle = new Beetle();
        beetle.setId(10L);
        beetle.setUser(owner);
        beetle.setSex(Sex.MALE.getLabel());
        beetle.setStage(Stage.ADULT.getLabel());
        beetle.setName("テスト個体");
    }

    private BeetleForm basicForm() {
        BeetleForm form = new BeetleForm();
        form.setName("更新個体");
        form.setSex(Sex.MALE);
        form.setStage(Stage.ADULT);
        return form;
    }

    // --- update 権限チェック ---

    @Test
    void update_投稿者本人は編集できる() {
        when(beetleRepository.findByIdWithUser(10L)).thenReturn(Optional.of(beetle));

        assertDoesNotThrow(() -> beetleService.update(10L, basicForm(), "owner"));
    }

    @Test
    void update_他人の生体は編集できない() {
        when(beetleRepository.findByIdWithUser(10L)).thenReturn(Optional.of(beetle));

        assertThatThrownBy(() -> beetleService.update(10L, basicForm(), "other"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("編集権限がありません");
    }

    // --- delete 権限チェック ---

    @Test
    void delete_投稿者本人は削除できる() {
        when(beetleRepository.findByIdWithUser(10L)).thenReturn(Optional.of(beetle));
        when(transactionRepository.findByBeetleWithUsers(beetle)).thenReturn(List.of());
        doNothing().when(beetleImageService).deleteAllForBeetle(beetle);

        assertDoesNotThrow(() -> beetleService.delete(10L, "owner"));
    }

    @Test
    void delete_他人の生体は削除できない() {
        when(beetleRepository.findByIdWithUser(10L)).thenReturn(Optional.of(beetle));

        assertThatThrownBy(() -> beetleService.delete(10L, "other"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("削除権限がありません");
    }

    // --- 父個体の性別バリデーション ---

    @Test
    void update_父個体にオスを設定できる() {
        Beetle father = new Beetle();
        father.setId(20L);
        father.setSex(Sex.MALE.getLabel());

        BeetleForm form = basicForm();
        form.setFatherId(20L);

        when(beetleRepository.findByIdWithUser(10L)).thenReturn(Optional.of(beetle));
        when(beetleRepository.findByIdWithUser(20L)).thenReturn(Optional.of(father));

        assertDoesNotThrow(() -> beetleService.update(10L, form, "owner"));
    }

    @Test
    void update_父個体にメスを設定するとBAD_REQUEST() {
        Beetle femaleBeetle = new Beetle();
        femaleBeetle.setId(20L);
        femaleBeetle.setSex(Sex.FEMALE.getLabel());

        BeetleForm form = basicForm();
        form.setFatherId(20L);

        when(beetleRepository.findByIdWithUser(10L)).thenReturn(Optional.of(beetle));
        when(beetleRepository.findByIdWithUser(20L)).thenReturn(Optional.of(femaleBeetle));

        assertThatThrownBy(() -> beetleService.update(10L, form, "owner"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("父個体にはオスの生体のみ選択できます");
    }

    // --- 母個体の性別バリデーション ---

    @Test
    void update_母個体にメスを設定できる() {
        Beetle mother = new Beetle();
        mother.setId(30L);
        mother.setSex(Sex.FEMALE.getLabel());

        BeetleForm form = basicForm();
        form.setMotherId(30L);

        when(beetleRepository.findByIdWithUser(10L)).thenReturn(Optional.of(beetle));
        when(beetleRepository.findByIdWithUser(30L)).thenReturn(Optional.of(mother));

        assertDoesNotThrow(() -> beetleService.update(10L, form, "owner"));
    }

    @Test
    void update_母個体にオスを設定するとBAD_REQUEST() {
        Beetle maleBeetle = new Beetle();
        maleBeetle.setId(30L);
        maleBeetle.setSex(Sex.MALE.getLabel());

        BeetleForm form = basicForm();
        form.setMotherId(30L);

        when(beetleRepository.findByIdWithUser(10L)).thenReturn(Optional.of(beetle));
        when(beetleRepository.findByIdWithUser(30L)).thenReturn(Optional.of(maleBeetle));

        assertThatThrownBy(() -> beetleService.update(10L, form, "owner"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("母個体にはメスの生体のみ選択できます");
    }

    // --- 自己参照バリデーション ---

    @Test
    void update_自分自身を父個体に設定するとBAD_REQUEST() {
        BeetleForm form = basicForm();
        form.setFatherId(10L);

        // 同じIDのbeetleが父個体候補として返る
        when(beetleRepository.findByIdWithUser(10L)).thenReturn(Optional.of(beetle));

        assertThatThrownBy(() -> beetleService.update(10L, form, "owner"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("自分自身を父個体に設定できません");
    }

    @Test
    void update_自分自身を母個体に設定するとBAD_REQUEST() {
        beetle.setSex(Sex.FEMALE.getLabel());
        BeetleForm form = basicForm();
        form.setSex(Sex.FEMALE);
        form.setMotherId(10L);

        when(beetleRepository.findByIdWithUser(10L)).thenReturn(Optional.of(beetle));

        assertThatThrownBy(() -> beetleService.update(10L, form, "owner"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("自分自身を母個体に設定できません");
    }

    // --- 羽化時期の時系列バリデーション ---

    @Test
    void update_父個体より前の羽化時期はBAD_REQUEST() {
        Beetle father = new Beetle();
        father.setId(20L);
        father.setSex(Sex.MALE.getLabel());
        father.setEmergenceDate("2024-06");

        BeetleForm form = basicForm();
        form.setFatherId(20L);
        form.setEmergenceDate("2024-03");

        when(beetleRepository.findByIdWithUser(10L)).thenReturn(Optional.of(beetle));
        when(beetleRepository.findByIdWithUser(20L)).thenReturn(Optional.of(father));

        assertThatThrownBy(() -> beetleService.update(10L, form, "owner"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("羽化時期は父個体の羽化時期より後の日付を入力してください");
    }

    @Test
    void update_母個体より前の羽化時期はBAD_REQUEST() {
        Beetle mother = new Beetle();
        mother.setId(30L);
        mother.setSex(Sex.FEMALE.getLabel());
        mother.setEmergenceDate("2024-06");

        BeetleForm form = basicForm();
        form.setMotherId(30L);
        form.setEmergenceDate("2024-03");

        when(beetleRepository.findByIdWithUser(10L)).thenReturn(Optional.of(beetle));
        when(beetleRepository.findByIdWithUser(30L)).thenReturn(Optional.of(mother));

        assertThatThrownBy(() -> beetleService.update(10L, form, "owner"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("羽化時期は母個体の羽化時期より後の日付を入力してください");
    }
}
