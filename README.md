# Vehicle Tracker

![Kotlin](https://img.shields.io/badge/Kotlin-2.2.0-blue?logo=kotlin)

---

## 📃 Descrição
O Vehicle Tracker é uma aplicação Kotlin que simula o movimento de veículos, gerenciando coordenadas, velocidades e notificações de defeitos. Ele se integra com o backend Supabase para enviar dados de veículos e coordenadas em tempo real, usando Coroutines para operações assíncronas e persistência local para estados. O projeto é projetado para trabalhar em conjunto com o Drive Manager (consulte o [repositório](https://github.com/FMoreiraSouza/DriveManager)), uma aplicação Flutter que consome os dados gerados pelo Vehicle Tracker para monitoramento e gerenciamento de frotas. O projeto segue uma **arquitetura em camadas** com influência de **Domain-Driven Design (DDD)**. Possui as camadas `domain` (modelos, casos de uso e interfaces de repositório), `application` (orquestração da lógica de negócio), `infrastructure` (implementações de API e persistência) e `presentation` (execução da simulação). Essa arquitetura promove modularidade, testabilidade e separação de responsabilidades, com forte ênfase em abstrações do domínio e injeção de dependências.

---

## 💻 Tecnologias Utilizadas
- **Kotlin**: Linguagem de programação principal.
- **Coroutines**: Para operações assíncronas.
- **Supabase**: Backend para dados e notificações em tempo real.
- **Moshi**: Serialização JSON.
- **OkHttp**: Requisições HTTP.

---

## 🛎️ Funcionalidades

- **Simulação de Movimento**: Atualiza coordenadas, velocidade e quilometragem.
- **Gestão de Defeitos**: Detecta defeitos, pausa veículos e envia notificações.
- **Persistência**: Salva estados em `vehicle_state.dat`.
- **Notificações**: Envia alertas de defeitos para o Supabase.
- **Paradas**: Simula pausas para abastecimento ou defeitos.

---

## 📱 Execução

- Simulação a cada 5 segundos via `Timer`.
- Validação de coordenadas em áreas de rota predefinidas.
- Dados enviados para o Supabase em tempo real para consumo pelo **Drive Manager**.

---

## ▶️ Como Rodar o Projeto

### Pré-requisitos
- **JDK 17** ou superior.
- **Kotlin 2.2.0**.
- Conta no [Supabase](https://supabase.com/).
- **IntelliJ IDEA** (versão recomendada: 2024.2 ou mais recente).

### Clone o repositório
- git clone https://github.com/seu-user/vehicle-simulation-app.git
- cd vehicle-simulation-app

### Configuração

- Configure SUPABASE_URL e SUPABASE_KEY em AppConfig.kt ou como variáveis de ambiente.
- Crie as tabelas no Supabase:
  - No painel do Supabase, acesse a seção SQL Editor e execute os seguintes scripts para criar as tabelas necessárias:
    ```bash
    sqlCREATE TABLE public.vehicles (
    id SERIAL NOT NULL,
    plate_number TEXT NULL,
    brand TEXT NULL,
    model TEXT NULL,
    mileage REAL NULL,
    imei BIGINT NULL,
    hasDefect BOOLEAN NULL DEFAULT false,
    CONSTRAINT vehicles_pkey PRIMARY KEY (id)
    ) TABLESPACE pg_default;

    CREATE TABLE public.vehicle_coordinates (
      id SERIAL NOT NULL,
      latitude DOUBLE PRECISION NOT NULL,
      longitude DOUBLE PRECISION NOT NULL,
      timestamp TIMESTAMP WITH TIME ZONE NULL DEFAULT now(),
      imei BIGINT NULL,
      isStopped BOOLEAN NULL DEFAULT true,
      speed DOUBLE PRECISION NULL,
      CONSTRAINT vehicle_coordinates_pkey PRIMARY KEY (id)
    ) TABLESPACE pg_default;
    
    CREATE TABLE public.notifications (
      id SERIAL NOT NULL,
      message TEXT NOT NULL,
      created_at TIMESTAMP WITHOUT TIME ZONE NULL DEFAULT now(),
      plate_number TEXT NULL,
      CONSTRAINT notifications_pkey PRIMARY KEY (id)
    ) TABLESPACE pg_default;
- Habilite o Row Level Security (RLS):
  - No Supabase Dashboard, vá para Database > Tables e selecione cada tabela (vehicles, vehicle_coordinates, notifications).
  - Ative o RLS para cada tabela clicando em Enable RLS.
- Execute o seguinte script SQL no SQL Editor para configurar as políticas de RLS, permitindo leitura, inserção e atualização para usuários autenticados:
  ```bash
  -- Política para a tabela vehicles
  CREATE POLICY "Allow all operations for public on vehicles" ON public.vehicles
  FOR ALL
  TO public
  USING (true)
  WITH CHECK (true);

  -- Política para a tabela vehicle_coordinates
  CREATE POLICY "Allow all operations for public on vehicle_coordinates" ON public.vehicle_coordinates
  FOR ALL
  TO public
  USING (true)
  WITH CHECK (true);

  -- Política para a tabela notifications
  CREATE POLICY "Allow all operations for public on notifications" ON public.notifications
  FOR ALL
  TO public
  USING (true)
  WITH CHECK (true);
- Habilite o Realtime:
  - Para ativar o Realtime nas tabelas vehicles, vehicle_coordinates e notifications, é necessário adicionar essas tabelas à publicação supabase_realtime (ou criar uma nova publicação, se preferir).
  - Execute o seguinte script SQL no SQL Editor do Supabase:
  ```bash
  sql-- Criar a publicação supabase_realtime (se ainda não existir)
  CREATE PUBLICATION supabase_realtime FOR TABLE public.vehicles, public.vehicle_coordinates, public.notifications;
  
  -- Caso a publicação já exista, adicione as tabelas à publicação existente
  ALTER PUBLICATION supabase_realtime ADD TABLE public.vehicles;
  ALTER PUBLICATION supabase_realtime ADD TABLE public.vehicle_coordinates;
  ALTER PUBLICATION supabase_realtime ADD TABLE public.notifications;

### Instale as dependências:

- Execute o comando para sincronizar as dependências:
  ```bash
  ./gradlew build

### Rode o servidor
- Clique em **Run** ou abra o terminal e use o comando:
  ```bash
  ./gradlew run
- O servidor iniciará, e a simulação atualizará coordenadas a cada 5 segundos, com logs exibidos no console.

 ## 🎥 Confira a Apresentação do Sistema

Confira a apresentação do sistema: [Apresentação](https://youtu.be/CRELJC7L0mc)
