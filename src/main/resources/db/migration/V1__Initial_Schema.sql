CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE role (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    role_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES role(id)
);

CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_username ON users(username);

CREATE TABLE competition (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_competition_status ON competition(status);
CREATE INDEX idx_competition_dates ON competition(start_date, end_date);

CREATE TABLE sales (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    amount NUMERIC(15, 2) NOT NULL,
    quantity INTEGER NOT NULL,
    positive_sale BOOLEAN NOT NULL,
    travel_quantity INTEGER NOT NULL,
    sale_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sale_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_sale_user_date ON sales(user_id, sale_date);
CREATE INDEX idx_sale_date ON sales(sale_date);

CREATE TABLE scores (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    competition_id UUID NOT NULL,
    sale_id UUID,
    points NUMERIC(10, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_score_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_score_competition FOREIGN KEY (competition_id) REFERENCES competition(id),
    CONSTRAINT fk_score_sale FOREIGN KEY (sale_id) REFERENCES sales(id)
);

CREATE INDEX idx_score_user ON scores(user_id);
CREATE INDEX idx_score_competition ON scores(competition_id);
CREATE INDEX idx_score_user_competition ON scores(user_id, competition_id);

CREATE TABLE ranking (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    competition_id UUID NOT NULL,
    user_id UUID NOT NULL,
    rank INTEGER NOT NULL,
    total_score NUMERIC(10, 2) NOT NULL,
    last_update TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ranking_competition FOREIGN KEY (competition_id) REFERENCES competition(id),
    CONSTRAINT fk_ranking_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_ranking_competition ON ranking(competition_id);
CREATE INDEX idx_ranking_competition_rank ON ranking(competition_id, rank);
CREATE INDEX idx_ranking_user ON ranking(user_id);

CREATE TABLE notification (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    competition_id UUID,
    type VARCHAR(20) NOT NULL,
    title VARCHAR(100) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_notification_competition FOREIGN KEY (competition_id) REFERENCES competition(id)
);

CREATE INDEX idx_notification_user ON notification(user_id);
CREATE INDEX idx_notification_user_read ON notification(user_id, is_read);
CREATE INDEX idx_notification_created ON notification(created_at);
