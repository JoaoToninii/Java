# Voice Budget AI 🎙️💰
> **API Inteligente de Orçamento e Finanças por Voz com Spring Boot e Spring AI**

Projeto desenvolvido para o desafio de evolução de uma API de orçamento que utiliza inteligência artificial para processar comandos de voz e texto relacionados a transações financeiras.

---

## 🌟 1. O Que o Projeto Faz

A aplicação implementa um fluxo completo de processamento de comandos financeiros em linguagem natural, permitindo que o usuário fale ou digite suas receitas, despesas e consultas:

```
[ Áudio do Usuário ]  
        ↓ (Upload Multipart)
[ Whisper STT (Speech-to-Text) ] 
        ↓ (Texto em Português)
[ Spring AI ChatClient + System Prompt ]
        ↓ (Tool Calling / Function Calling)
[ Execução Real no Banco H2 ] (Criar transação, calcular saldo, consultar extrato)
        ↓ (Resposta Estruturada + Verificação de Limites)
[ Síntese de Voz TTS (Text-to-Speech) ] 
        ↓ (MP3 / Áudio de Retorno)
[ Resposta ao Usuário + Trilha de Auditoria ]
```

### Principais Recursos:
- **Processamento de Áudio (STT)**: Transcreve gravações de voz usando o modelo Whisper da OpenAI.
- **Entendimento de Intenção & Tool Calling**: Usa o `ChatClient` do Spring AI com `@Tool` para chamar métodos Java reais com base no que o usuário disse.
- **Persistência de Dados**: Grava receitas, despesas, limites de orçamento e histórico de auditoria no banco H2.
- **Resposta por Voz (TTS)**: Gera áudio falado de retorno com a confirmação da operação.
- **Interface Web Interativa**: Painel embutido no próprio Spring Boot com gravador de microfone direto no navegador, cards de saldo em tempo real e visualização de limites orçamentários.

---

## 🚀 2. Como Executar a Aplicação

### Pré-requisitos
- **Java 21** instalado no sistema.
- Conexão com a internet (para baixar dependências na primeira execução).

### Opção A: Executar pelo Terminal (PowerShell / CMD)

Abra o terminal e navegue até a pasta do projeto:

```powershell
cd C:\Users\jgton\.gemini\antigravity-ide\scratch\voice-budget-api
```

*(Opcional)* Se você tiver uma chave da OpenAI, configure-a no terminal:
```powershell
$env:OPENAI_API_KEY="sua-chave-aqui"
```

Inicie o servidor Spring Boot:
```powershell
.\mvnw.cmd spring-boot:run
```

### Opção B: Execução com 1 Clique (Windows)
Basta dar dois cliques no arquivo:
👉 `run.bat` (localizado na raiz do projeto).

---

## 🖥️ 3. Acessando a Aplicação

Assim que o Spring Boot iniciar (`Started VoiceBudgetApiApplication in ... seconds`):

- **Interface Web com Gravador de Microfone:**
  👉 [http://localhost:8080/](http://localhost:8080/)
- **Console do Banco de Dados H2:**
  👉 [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
  - *JDBC URL:* `jdbc:h2:mem:budgetdb`
  - *Usuário:* `sa`
  - *Senha:* (em branco)

---

## 💡 4. Melhorias Implementadas (Evolução da API)

Em atendimento às **"Ideias Para Evoluir"** sugeridas no desafio, foram adicionadas 4 melhorias principais:

1. **Sistema de Alertas e Limites Orçamentários por Categoria (`OrcamentoService`)**:
   - Cada categoria de despesa (Alimentação, Transporte, Lazer, etc.) pode ter um limite mensal definido.
   - Quando o usuário adiciona uma despesa, a aplicação calcula o acumulado e a IA emite alertas automáticos se atingir 80% ou ultrapassar 100% do teto estipulado.
2. **Trilha de Auditoria e Telemetria (`RegistroAuditoria`)**:
   - Cada comando recebido (áudio ou texto) é auditado com: canal de entrada, transcrição, intenção identificada, ferramentas/funções Java acionadas, tempo de resposta em milissegundos e status.
3. **Interface Web Interativa com Microfone Integrado (`static/index.html`)**:
   - Permite gravar a voz diretamente pelo navegador usando a API de áudio HTML5, enviando o arquivo `.webm` para a API REST e reproduzindo na hora o áudio gerado pelo TTS.
4. **Modo Fallback / Simulação Autônomo**:
   - O projeto funciona perfeitamente **mesmo sem chave de API da OpenAI configurada**. Ele identifica automaticamente a ausência da chave e ativa o processador de linguagem natural e gerador de tom sonoro interno, permitindo avaliar 100% dos fluxos de negócio sem custo de tokens.

---

## 🛠️ 5. Tecnologias Utilizadas

- **Java 21 (LTS)**
- **Spring Boot 3.3.4**
- **Spring AI 1.0.0-M3** (OpenAI ChatClient, Whisper STT, TTS e Tool Calling)
- **Spring Data JPA** & **Hibernate**
- **Banco de Dados H2** (In-Memory com Console Web)
- **Bean Validation** (`jakarta.validation`)
- **Maven Wrapper** (`mvnw` / `mvnw.cmd`)
- **HTML5, CSS3 Glassmorphism & JavaScript Vanilla**

---

## 🧪 6. Como Testar o Fluxo Principal

### 1. Pela Interface Web
1. Abra `http://localhost:8080/`.
2. Clique em **"Gravar Comando de Voz"** e fale: *"Gastei 45 reais no almoço de hoje"*.
3. Clique em **"Parar Gravação"**.
4. Veja a transcrição, a ferramenta acionada (`registrarTransacao`), a atualização imediata dos cards de saldo e o áudio da IA respondendo!

### 2. Por Requisição REST (PowerShell / cURL)

**Registrar uma receita:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/voice/chat" -Method Post -ContentType "application/json" -Body '{"mensagem": "Recebi 3500 reais de salário hoje"}'
```

**Registrar uma despesa:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/voice/chat" -Method Post -ContentType "application/json" -Body '{"mensagem": "Gastei 80 reais no mercado com alimentação"}'
```

**Consultar saldo:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/voice/chat" -Method Post -ContentType "application/json" -Body '{"mensagem": "Qual é o meu saldo atual?"}'
```

**Consultar extrato via REST tradicional:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/transacoes" -Method Get
```

**Consultar trilha de auditoria:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auditoria" -Method Get
```

### 3. Testes Automatizados
```powershell
.\mvnw.cmd test
```

---

## 🎓 7. O Que Foi Aprendido no Desafio

1. **Separação de Responsabilidades**: A IA não altera o banco diretamente; ela apenas extrai intenções e parâmetros para invocar **Tools** controladas por regras de negócio e validações em Java.
2. **Tool Calling no Spring AI**: Como transformar métodos com `@Tool` em capacidades dinâmicas que o modelo LLM pode consultar e executar quando necessário.
3. **Fluxo Multimodal Completo**: Conexão entre entrada de áudio (STT Whisper) -> Raciocínio (ChatClient) -> Execução (JPA/Database) -> Saída falada (TTS).
4. **Resiliência e Fallback**: Desenvolver arquiteturas preparadas para operar tanto com APIs externas de IA quanto com mecanismos locais de fallback quando indisponíveis.
