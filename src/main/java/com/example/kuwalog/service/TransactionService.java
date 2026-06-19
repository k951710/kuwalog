package com.example.kuwalog.service;

import com.example.kuwalog.dto.TransactionForm;
import com.example.kuwalog.entity.Beetle;
import com.example.kuwalog.entity.Transaction;
import com.example.kuwalog.entity.User;
import com.example.kuwalog.repository.BeetleRepository;
import com.example.kuwalog.repository.ReviewRepository;
import com.example.kuwalog.repository.TransactionRepository;
import com.example.kuwalog.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final BeetleRepository beetleRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    public TransactionService(TransactionRepository transactionRepository,
                              BeetleRepository beetleRepository,
                              UserRepository userRepository,
                              ReviewRepository reviewRepository) {
        this.transactionRepository = transactionRepository;
        this.beetleRepository = beetleRepository;
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
    }

    @Transactional(readOnly = true)
    public Transaction findById(Long transactionId) {
        return transactionRepository.findByIdWithUsers(transactionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "譲渡記録が見つかりません"));
    }

    @Transactional(readOnly = true)
    public List<Transaction> findByBeetleId(Long beetleId) {
        Beetle beetle = getBeetle(beetleId);
        return transactionRepository.findByBeetleWithUsers(beetle);
    }

    @Transactional
    public Transaction register(Long beetleId, TransactionForm form, String fromUsername) {
        Beetle beetle = beetleRepository.findByIdWithUser(beetleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "生体が見つかりません"));

        if (!beetle.getUser().getUsername().equals(fromUsername)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "譲渡記録を登録できるのは生体の投稿者のみです");
        }

        User fromUser = userRepository.findByUsername(fromUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));

        User toUser = userRepository.findByUsername(form.getToUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "譲渡先ユーザーが見つかりません"));

        if (fromUser.getId().equals(toUser.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "自分自身に譲渡することはできません");
        }

        Transaction transaction = new Transaction();
        transaction.setBeetle(beetle);
        transaction.setFromUser(fromUser);
        transaction.setToUser(toUser);
        transaction.setTransferredOn(form.getTransferredOn());
        return transactionRepository.save(transaction);
    }

    @Transactional
    public void delete(Long beetleId, Long transactionId, String username) {
        Transaction transaction = transactionRepository.findByIdWithUsers(transactionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "譲渡記録が見つかりません"));

        if (transaction.getBeetle().getId() != beetleId) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        if (!transaction.getFromUser().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "削除権限がありません");
        }

        // FK制約のため、紐づく評価を先に削除してから取引を削除する
        reviewRepository.deleteByTransaction(transaction);
        transactionRepository.delete(transaction);
    }

    private Beetle getBeetle(Long beetleId) {
        return beetleRepository.findById(beetleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "生体が見つかりません"));
    }
}
