# Vehicle Tracker

![Kotlin](https://img.shields.io/badge/Kotlin-2.2.0-blue?logo=kotlin)

---

## 📃 Descrição

O **Vehicle Tracker** é uma aplicação Kotlin que simula o movimento de veículos, gerenciando coordenadas, velocidades e notificações de defeitos. Integra-se com o backend **Supabase** para dados de veículos e coordenadas, usando **Coroutines** para operações assíncronas e persistência local para estados.

O projeto segue a **Clean Architecture**, com camadas **domain** para modelos e casos de uso, **application** para orquestração), **infrastructure** para API e persistência, e **presentation** para a execução da simulação. A arquitetura promove modularidade e testabilidade.

---

## 💻 Tecnologias Utilizadas

- **Kotlin** → Linguagem de programação.
- **Coroutines** → Operações assíncronas.
- **Supabase** → Backend para dados e notificações.
- **Moshi** → Serialização JSON.
- **OkHttp** → Requisições HTTP.
- **Clean Architecture** → Separação de responsabilidades.

---

## 🛎️ Funcionalidades

- **Simulação de Movimento**: Atualiza coordenadas, velocidade e quilometragem.
- **Gestão de Defeitos**: Detecta defeitos, pausa veículos e envia notificações.
- **Persistência**: Salva estados em `vehicle_state.dat`.
- **Notificações**: Alertas de defeitos via Supabase.
- **Paradas**: Simula pausas para abastecimento ou defeitos.

---

## 📱 Execução

- Simulação a cada 5 segundos via `Timer`.
- Validação de coordenadas em áreas de rota predefinidas.

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
- Crie as tabelas vehicles, vehicle_coordinates, e notifications no Supabase.

### Instale as dependências:

- Execute o comando para sincronizar as dependências:
  ```bash
  ./gradlew build

### Rode o servidor
- Clique em **Run** ou abra o terminal e use o comando:
  ```bash
  ./gradlew run
- O servidor iniciará, e a simulação atualizará coordenadas a cada 5 segundos, com logs exibidos no console. 
