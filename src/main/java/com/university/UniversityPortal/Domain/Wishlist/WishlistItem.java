package com.university.UniversityPortal.Domain.Wishlist;

import lombok.Data;

@Data
public class WishlistItem {
    private Long wishlistItemId;
    private Long wishlistId;
    private Long offeringId;
}
