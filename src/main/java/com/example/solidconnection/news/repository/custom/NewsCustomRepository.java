package com.example.solidconnection.news.repository.custom;

import com.example.solidconnection.news.dto.NewsResponse;
import java.util.List;

public interface NewsCustomRepository {

    List<NewsResponse> findNewsByAuthorIdWithLikeStatus(long authorId, Long siteUserId);
}
