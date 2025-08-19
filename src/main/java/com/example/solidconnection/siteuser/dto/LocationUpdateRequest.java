package com.example.solidconnection.siteuser.dto;

import java.util.List;

public record LocationUpdateRequest(
        List<String> interestedRegions,
        List<String> interestedCountries
) {

}
