# Controle de EPI

Aplicativo Android em Java para o **Projeto 04 – Controle de EPI** da TechInova
(Desenvolvimento de Aplicativos Móveis · Apps para o chão de fábrica).

O técnico de segurança registra cada EPI entregue no celular, o colaborador confirma
o recebimento no próprio aparelho e o histórico fica guardado no Cloud Firestore,
pronto para a fiscalização.

## Integrantes

| Integrante | Responsável por |
|---|---|
| Kaique | Configuração do projeto, autenticação, perfis e navegação, modelos e regras do Firestore |
| Lucas | Lista de EPIs, cadastro de EPI, lista de colaboradores, cadastro de colaborador |
| Pedro | Registro de entrega, minhas entregas, confirmação de recebimento, ficha do colaborador |
| Vinicius | CA vencendo, busca por matrícula, controle de estoque, README final |

## O problema

Hoje o técnico de segurança anota numa ficha de papel cada luva, óculos e protetor
auricular entregue. Quando a fiscalização pede o histórico de um funcionário, a ficha
está incompleta ou sumiu. O colaborador assina a ficha "depois", ninguém sabe se ele
realmente recebeu o EPI, e ninguém percebe quando o Certificado de Aprovação (CA) de
um lote de luvas venceu — o EPI continua sendo entregue com CA vencido.

## A solução

| Perfil | O que faz no app |
|---|---|
| Técnico de segurança | Cadastra EPIs (CA e validade) e colaboradores, registra entregas, consulta a ficha de cada colaborador e a lista de CAs vencidos ou vencendo |
| Colaborador | Vê somente as próprias entregas e confirma o recebimento de cada uma |

O perfil fica no documento `usuarios/{uid}` (campo `perfil`) e é definido pelo grupo
no console do Firebase. O usuário nunca escolhe o próprio perfil pelo app.

## Histórias de usuário (ES1)

### C1 – Cadastro de EPI
Como **técnico de segurança**, quero cadastrar um EPI com descrição, número do CA e
validade do CA para controlar quais equipamentos posso entregar.

- **Dado** que estou na tela de novo EPI, **quando** preencho descrição, CA e validade e toco em Salvar, **então** o EPI aparece na lista de EPIs sem recarregar a tela.
- **Dado** que deixei a descrição ou o CA em branco, **quando** toco em Salvar, **então** o app mostra a mensagem de erro no campo e não grava nada.
- **Dado** que não escolhi a validade do CA, **quando** toco em Salvar, **então** o app pede a data de validade.

### C2 – Cadastro de colaborador ligado à conta
Como **técnico de segurança**, quero cadastrar o colaborador com nome, matrícula e setor,
ligado à conta dele no app, para que ele veja as próprias entregas.

- **Dado** que o colaborador já criou a conta e tem perfil colaborador, **quando** informo nome, matrícula, setor e o e-mail da conta, **então** o colaborador aparece na lista de colaboradores.
- **Dado** que informo um e-mail que não tem conta no app, **quando** toco em Salvar, **então** o app avisa que o colaborador precisa criar a conta primeiro.
- **Dado** que a matrícula já pertence a outro colaborador, **quando** toco em Salvar, **então** o app recusa e mostra de quem é a matrícula.

### C3 – Registro de entrega
Como **técnico de segurança**, quero registrar a entrega de um EPI (colaborador, EPI,
quantidade e data) para ter o histórico que a fiscalização exige.

- **Dado** que escolhi colaborador, EPI, quantidade e data, **quando** toco em Registrar entrega, **então** a entrega é gravada com status "Aguardando confirmação".
- **Dado** que digitei quantidade zero, vazia ou inválida, **quando** toco em Registrar entrega, **então** o app mostra o erro no campo e não grava.
- **Dado** que o CA do EPI escolhido está vencido, **quando** tento registrar a entrega, **então** o app recusa e pede para atualizar o CA.

### C4 – Colaborador vê e confirma as próprias entregas
Como **colaborador**, quero ver as entregas feitas para mim e confirmar o recebimento
para que o registro tenha a minha confirmação.

- **Dado** que o técnico registrou uma entrega para mim, **quando** abro o app, **então** a entrega aparece na minha lista sem eu precisar recarregar.
- **Dado** uma entrega aguardando confirmação, **quando** toco em Confirmar recebimento e confirmo, **então** ela passa para "Confirmado" com data e hora.
- **Dado** que sou colaborador, **quando** o app consulta entregas de outra pessoa, **então** as regras do Firestore negam a leitura.

### C5 – Ficha do colaborador
Como **técnico de segurança**, quero abrir a ficha de um colaborador com todas as
entregas e a situação de cada uma para responder à fiscalização.

- **Dado** que abro a ficha de um colaborador, **quando** a tela carrega, **então** vejo todas as entregas dele com quantidade, CA, data e status.
- **Dado** que o colaborador confirmou uma entrega no aparelho dele, **quando** estou com a ficha aberta, **então** o status muda para "Confirmado" sozinho.

### C6 – CA vencido ou vencendo
Como **técnico de segurança**, quero ver os EPIs com CA vencido ou que vence nos
próximos 30 dias para trocar o lote antes de entregar.

- **Dado** um EPI com validade do CA dentro dos próximos 30 dias, **quando** abro a tela de CA vencendo, **então** ele aparece com "Vence em X dias".
- **Dado** um EPI com CA já vencido, **quando** abro a tela, **então** ele aparece em vermelho com "Vencido há X dias".
- **Dado** um EPI com CA válido por mais de 30 dias, **quando** abro a tela, **então** ele não aparece na lista.

### Complementares
- **Motivo da troca** – Como técnico, quero informar o motivo da entrega (primeira entrega, desgaste, perda, tamanho errado) para entender por que os EPIs estão sendo trocados.
  - Dado que registro uma entrega, quando escolho o motivo, então ele aparece na ficha e na lista do colaborador.
  - Dado que não mudo o motivo, quando registro, então o motivo gravado é "Primeira entrega".
- **Busca por matrícula** – Como técnico, quero buscar o colaborador pela matrícula para achar a ficha rápido.
  - Dado que digito parte da matrícula, quando a lista filtra, então só aparecem os colaboradores cuja matrícula começa com o que digitei.
  - Dado que nenhuma matrícula corresponde, quando termino de digitar, então o app mostra "Nenhum colaborador encontrado".
- **Controle de estoque** – Como técnico, quero que cada entrega baixe o estoque do EPI para saber quando comprar mais.
  - Dado um EPI com estoque 10, quando registro a entrega de 2, então o estoque passa para 8.
  - Dado um EPI com estoque 1, quando tento entregar 3, então o app recusa com "Estoque insuficiente".

## Telas

| Login | Painel do técnico | EPIs | Novo EPI |
|---|---|---|---|
| ![Login](docs/prints/01-login.png) | ![Painel](docs/prints/02-painel-tecnico.png) | ![EPIs](docs/prints/03-epis.png) | ![Novo EPI](docs/prints/04-novo-epi.png) |

| Colaboradores | Ficha do colaborador | Registrar entrega | CA vencendo | Minhas entregas |
|---|---|---|---|---|
| ![Colaboradores](docs/prints/05-colaboradores.png) | ![Ficha](docs/prints/06-ficha.png) | ![Entrega](docs/prints/07-nova-entrega.png) | ![CA](docs/prints/08-ca-vencendo.png) | ![Minhas entregas](docs/prints/09-minhas-entregas.png) |

## Tecnologias

- Android nativo em **Java 17**, `minSdk 30`, Material Components
- **Firebase Authentication** (e-mail e senha)
- **Cloud Firestore** com snapshot listeners, transação e regras de segurança por perfil e por dono
- RecyclerView em todas as listas, com tela de estado vazio

## Estrutura do Firestore

| Coleção | Campos | Quem escreve |
|---|---|---|
| `usuarios/{uid}` | `nome`, `email`, `perfil` (`tecnico` ou `colaborador`) | o app cria `nome` e `email`; o **perfil é definido no console** |
| `epis/{id}` | `descricao`, `ca`, `validadeCa` (timestamp), `estoque` | técnico |
| `colaboradores/{uid}` | `nome`, `matricula`, `setor`, `email` (o id é o uid da conta do colaborador) | técnico |
| `entregas/{id}` | `colaboradorUid`, `colaboradorNome`, `epiId`, `epiDescricao`, `epiCa`, `quantidade`, `data`, `motivo`, `confirmado`, `confirmadoEm`, `tecnicoUid` | técnico cria; colaborador só altera `confirmado` e `confirmadoEm` |

A lista de CA vencendo usa uma **consulta por data**:
`epis.whereLessThan("validadeCa", hoje + 31 dias).orderBy("validadeCa")`.

## Regras de segurança

As regras estão em [`firestore.rules`](firestore.rules). Resumo:

- `usuarios`: cada um lê o próprio documento; o técnico lê todos. Na criação o app só pode gravar `nome` e `email` — **o campo `perfil` é bloqueado**, então ninguém se promove a técnico pelo app.
- `epis` e `colaboradores`: só o técnico escreve. O colaborador lê apenas o próprio cadastro.
- `entregas`: o técnico lê tudo e cria; o colaborador **só lê as entregas em que `colaboradorUid` é o uid dele** e só pode mudar `confirmado` de `false` para `true` (e `confirmadoEm`). Ninguém apaga entrega.

Teste para a apresentação: entrar como colaborador e tentar ler `entregas` de outra pessoa no
Rules Playground do console — a leitura é negada.

## Como configurar o Firebase

1. Crie um projeto no [console do Firebase](https://console.firebase.google.com).
2. Adicione um app Android com o pacote `br.senai.sp.controleepi`.
3. Baixe o `google-services.json` e coloque em `app/google-services.json`.
   **Esse arquivo fica fora do repositório** (está no `.gitignore`).
4. Em **Authentication › Sign-in method**, ative **E-mail/senha**.
5. Em **Firestore Database**, crie o banco (modo produção) e cole o conteúdo de
   `firestore.rules` na aba **Regras**. Clique em **Publicar**.
6. Abra o projeto no Android Studio, sincronize o Gradle e rode no emulador.

### Definindo o perfil de cada conta

1. Crie a conta pelo próprio app (tela **Ainda não tenho conta**). O app abre a tela "Perfil em análise".
2. No console, abra **Firestore › usuarios › {uid da conta}**.
3. Adicione o campo `perfil` (string) com o valor `tecnico` ou `colaborador`.
4. No app, toque em **Verificar novamente**.

Para o colaborador aparecer para o técnico, depois de definir o perfil `colaborador`, o técnico
cadastra o colaborador pelo app (tela **Colaboradores › Novo colaborador**) usando o e-mail da conta.

## Contas de teste

| Perfil | E-mail | Senha |
|---|---|---|
| Técnico de segurança | `tecnico.seguranca@techinova.com` | `Epi@2026` |
| Colaborador | `joao.silva@techinova.com` | `Epi@2026` |
| Colaborador | `maria.souza@techinova.com` | `Epi@2026` |

## Dados de exemplo para a demonstração

| EPI | CA | Validade do CA | Estoque |
|---|---|---|---|
| Luva nitrílica tam. 9 | 39136 | daqui a 15 dias (aparece em CA vencendo) | 40 |
| Óculos de proteção incolor | 10346 | daqui a 2 anos | 25 |
| Protetor auricular tipo plug | 5745 | vencido há 10 dias (aparece em vermelho) | 200 |
| Botina de segurança com bico de aço nº 42 | 41287 | daqui a 3 anos | 12 |

| Colaborador | Matrícula | Setor |
|---|---|---|
| João da Silva | 10234 | Estamparia |
| Maria Souza | 10457 | Solda |

## Roteiro da demonstração

1. O técnico registra a entrega de 1 par de **Luva nitrílica tam. 9** para João da Silva.
2. No aparelho do João, a entrega aparece sozinha e ele toca em **Confirmar recebimento**.
3. Na ficha do João, no aparelho do técnico, o status muda para **Confirmado**.
4. O técnico abre **CA vencido ou vencendo** e mostra a luva (vence em 15 dias) e o protetor (vencido).

## Checklist CORE e núcleo comum

| Requisito | Situação | Observação |
|---|---|---|
| NC1 – Login e cadastro, sessão mantida, sair no menu | Feito | |
| NC2 – Firestore com snapshot listener nas listas | Feito | EPIs, colaboradores, entregas, ficha e CA vencendo |
| NC3 – Regras de segurança com controle de perfil | Feito | `firestore.rules` |
| NC4 – Perfil em `usuarios/{uid}` definido no console | Feito | regra bloqueia o campo `perfil` na criação |
| NC5 – RecyclerView e estado vazio | Feito | |
| NC6 – Formulários validados em português | Feito | |
| NC7 – Girar a tela não perde o que foi digitado | Feito | datas e seleções salvas em `onSaveInstanceState` |
| NC8 – README completo, `google-services.json` fora do repo | Feito | |
| C1 – Cadastro de EPI com CA e validade | Feito | |
| C2 – Cadastro de colaborador ligado à conta | Feito | vínculo pelo e-mail da conta |
| C3 – Registro de entrega | Feito | |
| C4 – Colaborador vê só as próprias entregas e confirma | Feito | garantido pelas regras |
| C5 – Ficha do colaborador com status de confirmação | Feito | |
| C6 – Lista de CA vencido ou vencendo em 30 dias | Feito | consulta por data |
| Complementar – Motivo da troca | Feito | |
| Complementar – Controle de estoque | Feito | baixa em transação, recusa estoque insuficiente |
| Complementar – Busca por matrícula | Feito | |

## Próximos passos e limitações

- Foto do EPI e assinatura do colaborador usando Cloudinary.
- Notificação push quando uma entrega for registrada para o colaborador.
- Exportar a ficha do colaborador em PDF para a fiscalização.
- Limitação: o vínculo do colaborador depende de o perfil ter sido definido no console antes do cadastro.

## Fluxo de trabalho (ES2 e ES3)

- Quadro no GitHub Projects com as colunas **A fazer**, **Fazendo** e **Feito**.
- Cada história é uma issue com responsável. A issue só vai para Feito quando todos os critérios de aceite funcionam.
- Nada de commit direto na `main`: cada issue tem uma branch `feat/...` e entra por pull request revisado e aprovado por outro integrante. O PR cita a issue (`Closes #N`).
