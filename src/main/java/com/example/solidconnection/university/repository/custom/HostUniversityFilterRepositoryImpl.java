package com.example.solidconnection.university.repository.custom;

import com.example.solidconnection.location.country.domain.QCountry;
import com.example.solidconnection.location.region.domain.QRegion;
import com.example.solidconnection.university.domain.HostUniversity;
import com.example.solidconnection.university.domain.QHostUniversity;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
public class HostUniversityFilterRepositoryImpl implements HostUniversityFilterRepository {

    private final JPAQueryFactory queryFactory;

    @Autowired
    public HostUniversityFilterRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<HostUniversity> findAllBySearchCondition(
            String keyword,
            String countryCode,
            String regionCode,
            Pageable pageable
    ) {
        QHostUniversity hostUniversity = QHostUniversity.hostUniversity;
        QCountry country = QCountry.country;
        QRegion region = QRegion.region;

        List<HostUniversity> content = queryFactory
                .selectFrom(hostUniversity)
                .leftJoin(hostUniversity.country, country).fetchJoin()
                .leftJoin(hostUniversity.region, region).fetchJoin()
                .where(
                        keywordContains(hostUniversity, keyword),
                        countryCodeEq(country, countryCode),
                        regionCodeEq(region, regionCode)
                )
                .orderBy(hostUniversity.id.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(hostUniversity.count())
                .from(hostUniversity)
                .leftJoin(hostUniversity.country, country)
                .leftJoin(hostUniversity.region, region)
                .where(
                        keywordContains(hostUniversity, keyword),
                        countryCodeEq(country, countryCode),
                        regionCodeEq(region, regionCode)
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression keywordContains(QHostUniversity hostUniversity, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return hostUniversity.koreanName.contains(keyword)
                .or(hostUniversity.englishName.containsIgnoreCase(keyword));
    }

    private BooleanExpression countryCodeEq(QCountry country, String countryCode) {
        if (countryCode == null || countryCode.isBlank()) {
            return null;
        }
        return country.code.eq(countryCode);
    }

    private BooleanExpression regionCodeEq(QRegion region, String regionCode) {
        if (regionCode == null || regionCode.isBlank()) {
            return null;
        }
        return region.code.eq(regionCode);
    }
}
