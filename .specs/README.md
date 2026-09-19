# .specs/

Esta pasta guarda as specs de cada mudança planejada para o serviço.

Cada spec deve conter:

- **Contexto**: por que essa mudança é necessária, o que existe hoje.
- **Tarefa**: o que deve ser feito, de forma objetiva.
- **Regras**: restrições específicas dessa tarefa, além das regras gerais em `AGENTS.md`.
- **Definição de pronto**: critérios verificáveis para considerar a tarefa concluída (testes passando, comportamento esperado, etc.).

Esta pasta **não entra automaticamente no contexto** de nenhum agente. Ela só é lida quando o agente é explicitamente instruído a abrir uma spec específica.
