ALTER TABLE interested_country
ADD CONSTRAINT uk_site_user_country
UNIQUE (site_user_id, country_code);

ALTER TABLE interested_region
ADD CONSTRAINT uk_site_user_region
UNIQUE (site_user_id, region_code);
