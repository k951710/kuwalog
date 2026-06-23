package com.example.kuwalog.service;

import com.example.kuwalog.dto.BeetleForm;
import com.example.kuwalog.entity.Beetle;
import com.example.kuwalog.entity.User;
import com.example.kuwalog.entity.enums.Sex;
import com.example.kuwalog.entity.enums.Stage;
import com.example.kuwalog.repository.BeetleRepository;
import com.example.kuwalog.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BeetleServiceTest {

    @Mock
    private BeetleRepository beetleRepository;

    @Mock
    private UserRepository userRepository;

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
        beetle.setName("テスト個体");
        beetle.setSex(Sex.MALE.getLabel());
        beetle.setStage(Stage.ADULT.getLabel());
    }

    // --- update: 権限チェック ---

    @Test
    void update_投稿者本人は編集できる() {
        when(beetleRepository.findByIdWithUser(10L)).thenReturn(Optional.of(beetle));

        BeetleForm form = validForm();
        assertDoesNotThrow(() -> beetleService.update(10L, form, "owner"));
    }

    @Test
    void update_他人は編集できない() {
        when(beetleRepository.findByIdWithUser(10L)).thenReturn(Optional.of(beetle));

        BeetleForm form = validForm();
        assertThatThrownBy(() -> beetleService.update(10L, form, "other"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("編集権限がありません");
    }

    // --- delete: 権限チェック ---

    @Test
    void delete_投稿者本人は削除できる() {
        when(beetleRepository.findByIdWithUser(10L)).thenReturn(Optional.of(beetle));

        assertDoesNotThrow(() -> beetleService.delete(10L, "owner"));
    }

    @Test
    void delete_他人は削除できない() {
        when(beetleRepository.findByIdWithUser(10L)).thenReturn(Optional.of(beetle));

        assertThatThrownBy(() -> beetleService.delete(10L, "other"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("削除権限がありません");
    }

    // --- 父個体の性別バリデーション ---

    @Test
    void post_父個体にオスを指定すると正常に登録できる() {
        Beetle father = makeBeetle(20L, Sex.MALE);
        when(userRepository.findByUsername("owner")).thenReturn(Optional.of(owner));
        when(beetleRepository.findByIdWithUser(20L)).thenReturn(Optional.of(father));
        when(beetleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BeetleForm form = validForm();
        form.setFatherId(20L);
        assertDoesNotThrow(() -> beetleService.post(form, "owner"));
    }

    @Test
    void post_父個体にメスを指定するとBAD_REQUESTになる() {
        Beetle femaleFather = makeBeetle(20L, Sex.FEMALE);
        when(userRepository.findByUsername("owner")).thenReturn(Optional.of(owner));
        when(beetleRepository.findByIdWithUser(20L)).thenReturn(Optional.of(femaleFather));

        BeetleForm form = validForm();
        form.setFatherId(20L);
        assertThatThrownBy(() -> beetleService.post(form, "owner"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("父個体にはオスの生体のみ選択できます");
    }

    // --- 母個体の性別バリデーション ---

    @Test
    void post_母個体にメスを指定すると正常に登録できる() {
        Beetle mother = makeBeetle(21L, Sex.FEMALE);
        when(userRepository.findByUsername("owner")).thenReturn(Optional.of(owner));
        when(beetleRepository.findByIdWithUser(21L)).thenReturn(Optional.of(mother));
        when(beetleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BeetleForm form = validForm();
        form.setMotherId(21L);
        assertDoesNotThrow(() -> beetleService.post(form, "owner"));
    }

    @Test
    void post_母個体にオスを指定するとBAD_REQUESTになる() {
        Beetle maleMother = makeBeetle(21L, Sex.MALE);
        when(userRepository.findByUsername("owner")).thenReturn(Optional.of(owner));
        when(beetleRepository.findByIdWithUser(21L)).thenReturn(Optional.of(maleMother));

        BeetleForm form = validForm();
        form.setMotherId(21L);
        assertThatThrownBy(() -> beetleService.post(form, "owner"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("母個体にはメスの生体のみ選択できます");
    }

    // --- 自己参照バリデーション ---

    @Test
    void update_自分自身を父個体に設定するとBAD_REQUESTになる() {
        // ID=10の個体が自分自身(ID=10)を父に設定しようとするケース
        when(beetleRepository.findByIdWithUser(10L)).thenReturn(Optional.of(beetle));

        BeetleForm form = validForm();
        form.setFatherId(10L);
        assertThatThrownBy(() -> beetleService.update(10L, form, "owner"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("自分自身を父個体に設定できません");
    }

    @Test
    void update_自分自身を母個体に設定するとBAD_REQUESTになる() {
        // メス個体が自分自身を母に設定しようとするケース
        beetle.setSex(Sex.FEMALE.getLabel());
        when(beetleRepository.findByIdWithUser(10L)).thenReturn(Optional.of(beetle));

        BeetleForm form = validForm();
        form.setMotherId(10L);
        assertThatThrownBy(() -> beetleService.update(10L, form, "owner"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("自分自身を母個体に設定できません");
    }

    // --- ヘルパー ---

    private BeetleForm validForm() {
        BeetleForm form = new BeetleForm();
        form.setName("テスト");
        form.setSex(Sex.MALE);
        form.setStage(Stage.ADULT);
        return form;
    }

    private Beetle makeBeetle(Long id, Sex sex) {
        Beetle b = new Beetle();
        b.setId(id);
        b.setSex(sex.getLabel());
        b.setUser(owner);
        return b;
    }
}
