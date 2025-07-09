package org.netway.dongnehankki.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.netway.dongnehankki.store.domain.Store;
import org.netway.dongnehankki.user.domain.User;
import org.netway.dongnehankki.user.domain.User.Role;


@AllArgsConstructor
public class UserResponse {
    private Long userId;
    private String id;
    private String nickname;
    private Role role;
    private StoreInfo store;

    @Getter
    @AllArgsConstructor
    private static class StoreInfo {
        private Long storeId;
        private String name;
        static StoreInfo fromEntity(Store store) {
            return new StoreInfo(store.getStoreId(), store.getName());
        }
    }

    public static UserResponse fromEntity(User user){
        StoreInfo storeInfo = null;
        if (user.getRole() == Role.OWNER && user.getStore() != null){
            storeInfo = StoreInfo.fromEntity(user.getStore());
        }

        return new UserResponse(
            user.getUserId(),
            user.getId(),
            user.getNickname(),
            user.getRole(),
            storeInfo
        );
    }



}
