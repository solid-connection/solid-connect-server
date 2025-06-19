ALTER TABLE interested_country
ADD CONSTRAINT uk_interested_country_site_user_id_country_code
UNIQUE (site_user_id, country_code);

ALTER TABLE interested_region
ADD CONSTRAINT uk_interested_region_site_user_id_region_code
UNIQUE (site_user_id, region_code);
