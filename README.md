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

## Fluxo de trabalho (ES2 e ES3)

- Quadro no GitHub Projects com as colunas **A fazer**, **Fazendo** e **Feito**.
- Cada história é uma issue com responsável. A issue só vai para Feito quando todos os critérios de aceite funcionam.
- Nada de commit direto na `main`: cada issue tem uma branch `feat/...` e entra por pull request revisado e aprovado por outro integrante. O PR cita a issue (`Closes #N`).
