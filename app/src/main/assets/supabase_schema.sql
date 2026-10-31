-- Matematik Macerası Supabase Veritabanı Şeması

-- Kullanıcılar tablosu
CREATE TABLE users (
  id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
  username TEXT,
  avatar_url TEXT,
  total_stars INTEGER DEFAULT 0,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Oyun ilerlemesi tablosu
CREATE TABLE game_progress (
  id SERIAL PRIMARY KEY,
  user_id UUID REFERENCES users(id) ON DELETE CASCADE,
  game_id TEXT NOT NULL,
  level INTEGER NOT NULL DEFAULT 1,
  score INTEGER NOT NULL DEFAULT 0,
  stars INTEGER NOT NULL DEFAULT 0,
  last_updated TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  UNIQUE(user_id, game_id)
);

-- RLS (Row Level Security) Politikaları

-- Kullanıcılar tablosu için RLS
ALTER TABLE users ENABLE ROW LEVEL SECURITY;

-- Kullanıcılar kendi verilerini okuyabilir
CREATE POLICY "Kullanıcılar kendi verilerini okuyabilir" ON users
  FOR SELECT USING (auth.uid() = id);

-- Kullanıcılar kendi verilerini güncelleyebilir
CREATE POLICY "Kullanıcılar kendi verilerini güncelleyebilir" ON users
  FOR UPDATE USING (auth.uid() = id);

-- Kullanıcılar kendi verilerini ekleyebilir
CREATE POLICY "Kullanıcılar kendi verilerini ekleyebilir" ON users
  FOR INSERT WITH CHECK (auth.uid() = id);

-- Oyun ilerlemesi tablosu için RLS
ALTER TABLE game_progress ENABLE ROW LEVEL SECURITY;

-- Kullanıcılar kendi oyun ilerlemelerini okuyabilir
CREATE POLICY "Kullanıcılar kendi oyun ilerlemelerini okuyabilir" ON game_progress
  FOR SELECT USING (auth.uid() = user_id);

-- Kullanıcılar kendi oyun ilerlemelerini güncelleyebilir
CREATE POLICY "Kullanıcılar kendi oyun ilerlemelerini güncelleyebilir" ON game_progress
  FOR UPDATE USING (auth.uid() = user_id);

-- Kullanıcılar kendi oyun ilerlemelerini ekleyebilir
CREATE POLICY "Kullanıcılar kendi oyun ilerlemelerini ekleyebilir" ON game_progress
  FOR INSERT WITH CHECK (auth.uid() = user_id);

-- Kullanıcılar kendi oyun ilerlemelerini silebilir
CREATE POLICY "Kullanıcılar kendi oyun ilerlemelerini silebilir" ON game_progress
  FOR DELETE USING (auth.uid() = user_id);

-- Tetikleyiciler (Triggers)

-- Kullanıcı güncellendiğinde updated_at alanını güncelle
CREATE OR REPLACE FUNCTION update_users_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_users_updated_at
BEFORE UPDATE ON users
FOR EACH ROW
EXECUTE FUNCTION update_users_updated_at();

-- Oyun ilerlemesi güncellendiğinde last_updated alanını güncelle
CREATE OR REPLACE FUNCTION update_game_progress_last_updated()
RETURNS TRIGGER AS $$
BEGIN
  NEW.last_updated = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_game_progress_last_updated
BEFORE UPDATE ON game_progress
FOR EACH ROW
EXECUTE FUNCTION update_game_progress_last_updated();

-- Toplam yıldız sayısını hesaplayan fonksiyon
CREATE OR REPLACE FUNCTION calculate_total_stars(user_uuid UUID)
RETURNS INTEGER AS $$
DECLARE
  total INTEGER;
BEGIN
  SELECT COALESCE(SUM(stars), 0) INTO total
  FROM game_progress
  WHERE user_id = user_uuid;
  
  RETURN total;
END;
$$ LANGUAGE plpgsql;

-- Oyun ilerlemesi eklendiğinde veya güncellendiğinde toplam yıldız sayısını güncelle
CREATE OR REPLACE FUNCTION update_total_stars()
RETURNS TRIGGER AS $$
BEGIN
  UPDATE users
  SET total_stars = calculate_total_stars(NEW.user_id)
  WHERE id = NEW.user_id;
  
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_total_stars_on_insert
AFTER INSERT ON game_progress
FOR EACH ROW
EXECUTE FUNCTION update_total_stars();

CREATE TRIGGER update_total_stars_on_update
AFTER UPDATE OF stars ON game_progress
FOR EACH ROW
EXECUTE FUNCTION update_total_stars(); 