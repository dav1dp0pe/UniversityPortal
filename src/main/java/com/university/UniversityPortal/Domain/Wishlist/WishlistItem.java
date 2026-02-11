package com.university.UniversityPortal.Domain.Wishlist;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WishlistItem {
    private Long wishlistItemId;
    private Long wishlistId;
    private Long offeringId;
    private LocalDateTime addedAt;
}
