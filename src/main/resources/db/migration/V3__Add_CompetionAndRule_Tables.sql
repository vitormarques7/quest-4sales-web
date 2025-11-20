CREATE TABLE prize (
                       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                       competition_id UUID NOT NULL,
                       title VARCHAR(100) NOT NULL,
                       description TEXT,
                       image_url VARCHAR(255),
                       placement INTEGER NOT NULL,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       CONSTRAINT fk_prize_competition FOREIGN KEY (competition_id) REFERENCES competition(id)
);

CREATE TABLE rule (
                      id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                      competition_id UUID NOT NULL,
                      value_weight NUMERIC(10, 2) NOT NULL,
                      items_weight NUMERIC(10, 2) NOT NULL,
                      positivation_weight NUMERIC(10, 2) NOT NULL,
                      trip_weight NUMERIC(10, 2) NOT NULL,
                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      CONSTRAINT fk_rule_competition FOREIGN KEY (competition_id) REFERENCES competition(id)
);