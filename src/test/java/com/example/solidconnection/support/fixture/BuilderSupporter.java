package com.example.solidconnection.support.fixture;

import com.example.solidconnection.application.repository.ApplicationRepository;
import com.example.solidconnection.community.board.repository.BoardRepository;
import com.example.solidconnection.community.post.repository.PostImageRepository;
import com.example.solidconnection.community.post.repository.PostRepository;
import com.example.solidconnection.repositories.CountryRepository;
import com.example.solidconnection.repositories.RegionRepository;
import com.example.solidconnection.score.repository.GpaScoreRepository;
import com.example.solidconnection.score.repository.LanguageTestScoreRepository;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.university.repository.LanguageRequirementRepository;
import com.example.solidconnection.university.repository.UniversityInfoForApplyRepository;
import com.example.solidconnection.university.repository.UniversityRepository;
import org.springframework.stereotype.Component;

@Component
public class BuilderSupporter {

    private final SiteUserRepository siteUserRepository;
    private final RegionRepository regionRepository;
    private final CountryRepository countryRepository;
    private final UniversityRepository universityRepository;
    private final UniversityInfoForApplyRepository universityInfoForApplyRepository;
    private final LanguageRequirementRepository languageRequirementRepository;
    private final ApplicationRepository applicationRepository;
    private final GpaScoreRepository gpaScoreRepository;
    private final LanguageTestScoreRepository languageTestScoreRepository;
    private final BoardRepository boardRepository;
    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;

    public BuilderSupporter(
            SiteUserRepository siteUserRepository,
            RegionRepository regionRepository,
            CountryRepository countryRepository,
            UniversityRepository universityRepository,
            UniversityInfoForApplyRepository universityInfoForApplyRepository,
            LanguageRequirementRepository languageRequirementRepository,
            ApplicationRepository applicationRepository,
            GpaScoreRepository gpaScoreRepository,
            LanguageTestScoreRepository languageTestScoreRepository,
            BoardRepository boardRepository,
            PostRepository postRepository,
            PostImageRepository postImageRepository) {
        this.siteUserRepository = siteUserRepository;
        this.regionRepository = regionRepository;
        this.countryRepository = countryRepository;
        this.universityRepository = universityRepository;
        this.universityInfoForApplyRepository = universityInfoForApplyRepository;
        this.languageRequirementRepository = languageRequirementRepository;
        this.applicationRepository = applicationRepository;
        this.gpaScoreRepository = gpaScoreRepository;
        this.languageTestScoreRepository = languageTestScoreRepository;
        this.boardRepository = boardRepository;
        this.postRepository = postRepository;
        this.postImageRepository = postImageRepository;
    }

    public SiteUserRepository siteUserRepository() {
        return siteUserRepository;
    }

    public RegionRepository regionRepository() {
        return regionRepository;
    }

    public CountryRepository countryRepository() {
        return countryRepository;
    }

    public UniversityRepository universityRepository() {
        return universityRepository;
    }

    public UniversityInfoForApplyRepository universityInfoForApplyRepository() {
        return universityInfoForApplyRepository;
    }

    public LanguageRequirementRepository languageRequirementRepository() {
        return languageRequirementRepository;
    }

    public ApplicationRepository applicationRepository() {
        return applicationRepository;
    }

    public GpaScoreRepository gpaScoreRepository() {
        return gpaScoreRepository;
    }

    public LanguageTestScoreRepository languageTestScoreRepository() {
        return languageTestScoreRepository;
    }

    public BoardRepository boardRepository() {
        return boardRepository;
    }

    public PostRepository postRepository() {
        return postRepository;
    }

    public PostImageRepository postImageRepository() {
        return postImageRepository;
    }
}
