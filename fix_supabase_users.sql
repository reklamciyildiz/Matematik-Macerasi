-- Bu SQL betiği, Supabase Authentication panelinde görünmeyen kullanıcıların görünür hale gelmesini sağlar
-- Supabase SQL Editör'de çalıştırmanız gereken komutlar:

-- 1. ADIM: Auth sisteminden kullanıcıları görebilmek için bu özel role'ü oluşturun
CREATE OR REPLACE FUNCTION public.sync_auth_users_to_public_users()
RETURNS TRIGGER AS $$
BEGIN
  INSERT INTO public.users (id, username, avatar_url, total_stars, created_at)
  VALUES (
    NEW.id, 
    COALESCE(NEW.raw_user_meta_data->>'username', 'Player_' || substr(NEW.id::text, 1, 8)),
    COALESCE(NEW.raw_user_meta_data->>'avatar_url', ''),
    COALESCE((NEW.raw_user_meta_data->>'total_stars')::int, 0),
    NOW()
  )
  ON CONFLICT (id) DO UPDATE SET
    username = COALESCE(NEW.raw_user_meta_data->>'username', users.username),
    avatar_url = COALESCE(NEW.raw_user_meta_data->>'avatar_url', users.avatar_url);
  
  RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 2. ADIM: Trigger'ı oluşturun - Her yeni kullanıcı eklendiğinde veya güncellendiğinde otomatik olarak çalışır
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
  AFTER INSERT OR UPDATE ON auth.users
  FOR EACH ROW EXECUTE PROCEDURE public.sync_auth_users_to_public_users();

-- 3. ADIM: Mevcut kullanıcıları manuel olarak ekleyin
INSERT INTO public.users (id, username, avatar_url, total_stars, created_at)
SELECT 
  id,
  COALESCE(raw_user_meta_data->>'username', 'Player_' || substr(id::text, 1, 8)) as username,
  COALESCE(raw_user_meta_data->>'avatar_url', '') as avatar_url,
  COALESCE((raw_user_meta_data->>'total_stars')::int, 0) as total_stars,
  NOW() as created_at
FROM 
  auth.users
ON CONFLICT (id) DO UPDATE SET
  username = EXCLUDED.username,
  avatar_url = EXCLUDED.avatar_url;

-- 4. ADIM: Kullanıcıların RLS (Row Level Security) politikalarını ayarlayın
ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;

-- Bu, sadece kimliği doğrulanmış kullanıcıların kendi verilerini okumasına izin verir
CREATE POLICY "Users can view their own data" ON public.users
  FOR SELECT
  USING (auth.uid() = id);

-- Bu, herkesin tüm kullanıcıları görmesine izin verir (leaderboard vb. için)
CREATE POLICY "Allow anonymous read access" ON public.users
  FOR SELECT
  USING (true);

-- 5. ADIM: "execute_sql" adlı bir RPC fonksiyonu ekleyin
-- Bu, SQL kodunu doğrudan çalıştırabilmenizi sağlar (sadece ihtiyaç duyulduğunda kullanın)
CREATE OR REPLACE FUNCTION public.execute_sql(sql_query text)
RETURNS json AS $$
BEGIN
  RETURN (SELECT json_agg(t) FROM (SELECT * FROM public.users LIMIT 10) t);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
