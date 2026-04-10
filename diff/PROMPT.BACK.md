**INSTRUCCIONES:**
1. Comienza leyendo el archivo `docs/sprints/backend-plan-sprint1.5.md` para entender tus fases.
2. Importante: Si aún no detectas tu archivo base CLAUDE.md leelo e inyecta el contexto
3. Consulta el esquema de bases de datos `docs/schema/database-schema.md` y guíate por `docs/GUIDE.md` para ir a las reglas de negocio en `docs/modules/` SOLO para las entidades que correspondan a refactorizar. No pierdas contexto leyendo frontend, UI, o entidades no afectadas.
4. Utiliza tus herramientas MCP de Supabase para leer el esquema en vivo de la DB (`newversion`), para asegurarte de que lo que vayas a refactorizar localmente hace match 1:1 con la DB de la nube. La idea es abrazar la nueva base de datos
5. Ten siempre presente el contexto de Flyway: la BD en prod se levantará directo en Supabase y tiene baseline 0, así que los scripts alterados deben usar `CREATE TABLE IF NOT EXISTS` u otras técnicas idempotentes.
6. Al final, incluye una lista de preguntas técnicas o dudas de implementación para mí, así podemos resolver los bloqueos ANTES de tocar el código.
7. Es posible que el plan o las instrucciones estén en español, pero la documentación del proyecto es escrictamente en ingles, por lo que debes entender ambos idiomas.
8. No trates de hacerlo todo de un solo, ve paso a paso y lleva tu control de cambios y ve marcando que ya fue refactorizado, para llevar un control y que yo sepa por donde vamos por si ocurre un error.
9. Ten en cuenta que se manejan dos perfiles, dev y prod. Con dev se levanta un compose de manera local y con prod se levanta directo en Supabase. Entonces por eso en dev debe permitir el create table if not exists y lo demás que encuentres en Supabase.