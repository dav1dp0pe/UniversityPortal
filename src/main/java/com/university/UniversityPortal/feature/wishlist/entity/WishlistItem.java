package com.university.UniversityPortal.feature.wishlist.entity;

import lombok.Data;

@Data
public class WishlistItem {
    private Long wishlistItemId;
    private Long wishlistId;
    private Long offeringId;
}
