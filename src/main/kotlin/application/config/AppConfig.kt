package application.config

object AppConfig {
    val supabaseUrl: String = System.getenv("SUPABASE_URL")
        ?: "https://iiyktpznaswcwtrgqmrc.supabase.co/rest/v1"
    val supabaseKey: String = System.getenv("SUPABASE_KEY")
        ?: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImlpeWt0cHpuYXN3Y3d0cmdxbXJjIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTYyMTkzNTgsImV4cCI6MjA3MTc5NTM1OH0.Do1wu7xJtBrR8QsksjDMkCuczjDf9ixK6tU_tkjfqAk"
}