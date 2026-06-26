package com.example.kuwalog.service;

import com.example.kuwalog.entity.Beetle;
import com.example.kuwalog.entity.Favorite;
import com.example.kuwalog.entity.User;
import java.util.List;
import com.example.kuwalog.repository.BeetleRepository;
import com.example.kuwalog.repository.FavoriteRepository;
import com.example.kuwalog.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final BeetleRepository beetleRepository;

    public FavoriteService(FavoriteRepository favoriteRepository,
                           UserRepository userRepository,
                           BeetleRepository beetleRepository) {
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.beetleRepository = beetleRepository;
    }

    @Transactional
    public void toggle(Long beetleId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Beetle beetle = beetleRepository.findById(beetleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        favoriteRepository.findByUserIdAndBeetleId(user.getId(), beetleId)
                .ifPresentOrElse(
                        favoriteRepository::delete,
                        () -> {
                            Favorite fav = new Favorite();
                            fav.setUser(user);
                            fav.setBeetle(beetle);
                            favoriteRepository.save(fav);
                        }
                );
    }

    @Transactional(readOnly = true)
    public long countByBeetleId(Long beetleId) {
        return favoriteRepository.countByBeetleId(beetleId);
    }

    @Transactional(readOnly = true)
    public List<Beetle> findFavoritedBeetles(String username) {
        return userRepository.findByUsername(username)
                .map(u -> favoriteRepository.findByUserIdWithBeetleOrderByCreatedAtDesc(u.getId())
                        .stream().map(Favorite::getBeetle).toList())
                .orElse(List.of());
    }

    @Transactional(readOnly = true)
    public boolean isFavorited(Long beetleId, String username) {
        if (username == null) return false;
        return userRepository.findByUsername(username)
                .map(u -> favoriteRepository.findByUserIdAndBeetleId(u.getId(), beetleId).isPresent())
                .orElse(false);
    }
}
