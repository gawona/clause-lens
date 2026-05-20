from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    app_name: str = "ClauseLens Analysis Engine"
    app_env: str = "local"

    class Config:
        env_file = ".env"


settings = Settings()