-------------- =================================================================
-- SCRIPT COMPLETO PARA CREAR Y POBLAR LA BASE DE DATOS DE HYPATIA
-- =================================================================

-- ========= SECCIÓN 1: DEFINICIÓN DE TABLAS (DDL) =========

-- Eliminar tablas si ya existen para una ejecución limpia
DROP TABLE IF EXISTS opciones_pregunta;
DROP TABLE IF EXISTS preguntas ;
DROP TABLE IF EXISTS cuestionario_secciones;
DROP TABLE IF EXISTS cuestionarios;
DROP TABLE IF EXISTS respuestas_usuario;
DROP TABLE IF EXISTS user_profile_movement_reasons;
DROP TABLE IF EXISTS user_profile_target_jobs;
DROP TABLE IF EXISTS user_profiles;
DROP TABLE IF EXISTS users;

-- Tabla principal para definir los cuestionarios o escenarios.
CREATE TABLE cuestionarios (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT,
    fase_cuestionario VARCHAR(50) NOT NULL,
    fecha_creacion TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN DEFAULT TRUE
);

-- Tabla para definir las secciones dentro de un cuestionario.
CREATE TABLE cuestionario_secciones (
    id SERIAL PRIMARY KEY,
    cuestionario_id INTEGER NOT NULL REFERENCES cuestionarios(id) ON DELETE CASCADE,
    nombre_seccion VARCHAR(255) NOT NULL,
    orden INTEGER NOT NULL
);

-- Tabla para almacenar cada una de las preguntas.
CREATE TABLE preguntas (
    id SERIAL PRIMARY KEY,
    cuestionario_id INTEGER NOT NULL REFERENCES cuestionarios(id) ON DELETE CASCADE,
    seccion_id INTEGER NOT NULL REFERENCES cuestionario_secciones(id) ON DELETE CASCADE,
    texto_pregunta TEXT NOT NULL,
    texto_ayuda TEXT,
    tipo_pregunta VARCHAR(50) NOT NULL,
    orden INTEGER NOT NULL
);

-- Tabla para las opciones de respuesta en preguntas de tipo selector.
CREATE TABLE opciones_pregunta (
    id SERIAL PRIMARY KEY,
    pregunta_id INTEGER NOT NULL REFERENCES preguntas(id) ON DELETE CASCADE,
    texto_opcion VARCHAR(255) NOT NULL,
    valor_opcion VARCHAR(255),
    orden INTEGER NOT NULL
);


-- ========= SECCIÓN 2: TABLAS DE USUARIOS Y PERFILES =========

-- Tabla para la información básica de los usuarios, incluyendo rol y estado
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'ROLE_PARTICIPANT',
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING_ONBOARDING',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Tabla para la información extendida del perfil de usuario
CREATE TABLE user_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    gender VARCHAR(50),
    cv_path VARCHAR(500),
    is_caregiver BOOLEAN,
    caregiving_hours_per_week VARCHAR(50),
    initial_education VARCHAR(50),
    higher_education_area VARCHAR(100),
    technology_language VARCHAR(100),
    years_of_experience VARCHAR(20),
    started_in_tech VARCHAR(100),
    current_position VARCHAR(100),
    work_mode VARCHAR(20),
    salary_range VARCHAR(50),
    expected_salary VARCHAR(50),
    has_completed_courses BOOLEAN,
    projects_built INTEGER,
    last_feedback TEXT,
    desired_position VARCHAR(100),
    daily_tasks TEXT,
    soft_skills TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Tablas auxiliares para las listas de strings en UserProfile
CREATE TABLE user_profile_movement_reasons (
    user_profile_id BIGINT NOT NULL REFERENCES user_profiles(id) ON DELETE CASCADE,
    reason VARCHAR(100)
);

CREATE TABLE user_profile_target_jobs (
    user_profile_id BIGINT NOT NULL REFERENCES user_profiles(id) ON DELETE CASCADE,
    job_type VARCHAR(100)
);

-- Tabla para almacenar las respuestas de los usuarios a las preguntas
CREATE TABLE respuestas_usuario (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    pregunta_id BIGINT NOT NULL REFERENCES preguntas(id) ON DELETE CASCADE,
    respuesta_texto TEXT,
    session_id VARCHAR(100),
    is_current BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ai_interactions(
    ai_interaction_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    interaction_type VARCHAR(50) not null,
    request_payload TEXT not null,
    response_payload TEXT not null,
    cache_key VARCHAR(125),
    is_cached_response BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);


-- ========= SECCIÓN 3: INSERCIÓN DE DATOS DEL CUESTIONARIO =========

-- --- Cuestionario ---
INSERT INTO cuestionarios (id, nombre, descripcion, fase_cuestionario) VALUES
(1, 'Diagnóstico de Onboarding', 'Cuestionario inicial para recopilar información del perfil de la participante.', 'onboarding');

-- --- Secciones ---
INSERT INTO cuestionario_secciones (id, cuestionario_id, nombre_seccion, orden) VALUES
(10, 1, 'Información Personal', 1),
(11, 1, 'Información Escolar', 2),
(12, 1, 'Información complementaria laboral', 3),
(13, 1, 'Actividades de Cuidado', 4),
(14, 1, 'Autoevaluación de Habilidades', 5);

-- --- Preguntas ---
INSERT INTO preguntas (id, cuestionario_id, seccion_id, texto_pregunta, texto_ayuda, tipo_pregunta, orden) VALUES
-- Sección: Información Personal
(101, 1, 10, 'Email de contacto', 'Asegúrate de que sea el mismo con el que te registraste. Usaremos este email para todas las comunicaciones oficiales.', 'email', 1),
(102, 1, 10, 'Nombre', NULL, 'texto_corto', 2),
(103, 1, 10, 'Apellidos', NULL, 'texto_corto', 3),
(104, 1, 10, 'Género', NULL, 'selector_simple', 4),
(105, 1, 10, 'Selecciona tu rango de edad', NULL, 'selector_simple', 5),
(107, 1, 10, 'Sube tu CV actualizado', 'Asegúrate de que esté en formato PDF y no pese más de 5MB.', 'archivo', 6),

-- Sección: Información Escolar
(106, 1, 11, 'Ultimo grado de estudios completo', 'Indica el nivel más alto que has completado y del cual tienes un certificado o título.', 'selector_simple', 1),
(108, 1, 11, '¿En qué área es tu nivel educativo superior?', 'Esta pregunta aparecerá solo si seleccionaste "Superior" o un nivel mayor en la pregunta anterior.', 'selector_simple', 2),
(109, 1, 11, 'Si no tienes estudios superiores, ¿Qué tecnología - lenguaje manejas actualmente?', 'Puedes marcar más de uno. Esta pregunta aparecerá solo si no seleccionaste un grado de estudios superior.', 'selector_multiple', 3),

-- Sección: Información complementaria laboral
(110, 1, 12, '¿Actualmente te encuentras trabajando?', NULL, 'selector_simple', 1),
(111, 1, 12, 'Si la respuesta es sí ¿Cuántas horas trabajas a la semana?', 'Esta pregunta aparecerá solo si tu respuesta anterior fue "Sí".', 'selector_simple', 2),
(112, 1, 12, 'Tu trabajo es:', 'Esta pregunta aparecerá solo si tu respuesta anterior fue "Sí".', 'selector_simple', 3),
(113, 1, 12, '¿Qué posición tienes actualmente?', 'Esta pregunta aparecerá solo si tu respuesta anterior fue "Sí".', 'selector_simple', 4),
(114, 1, 12, 'Indica tu rango salarial', 'Esta pregunta aparecerá solo si tu respuesta anterior fue "Sí".', 'selector_simple', 5),
(115, 1, 12, 'Actualmente buscas moverte hacia otro espacio laboral, ¿Cuál dirías que es la principal razón?', 'Esta pregunta aparecerá solo si tu respuesta anterior fue "Sí".', 'selector_simple', 6),
(116, 1, 12, 'Al cambiarte de empleo, esperarías ganar en promedio cuánto más de lo que percibes actualmente, por mes:', 'Esta pregunta aparecerá solo si actualmente te encuentras trabajando.', 'selector_simple', 7),
(117, 1, 12, 'En los últimos seis meses has completado cursos, talleres, diplomados de cualquier índole:', NULL, 'selector_simple', 8),
(118, 1, 12, '¿Cuántos proyectos has construido con esos cursos, talleres o diplomados? en los últimos dos a tres meses:', 'Esta pregunta aparecerá solo si tu respuesta anterior fue "Sí".', 'selector_simple', 9),
(119, 1, 12, 'Comparte la última retroalimentación profesional de habilidades y competencias NO técnicas que algún superior, colega o RH te brindó. Describe de forma específica y clara.', NULL, 'texto_largo', 10),
(120, 1, 12, 'Coloca el nombre de tres vacantes a las que te gustaría aplicar actualmente', NULL, 'texto_largo', 11),

-- Sección: Actividades de Cuidado
(121, 1, 13, '¿Participas activamente en el cuidado de infantes, personas mayores o con alguna discapacidad?', NULL, 'selector_simple', 1),
(122, 1, 13, 'Si la respuesta fue que sí, ¿Cuánto tiempo dedicas a ello por semana?', '(O podrías colocar un aproximado de tu tiempo al día en porcentaje)', 'selector_simple', 2),
(123, 1, 13, 'El tiempo que dedicas al cuidado ¿En qué cantidad de horas incrementa a tu trabajo formal a la semana?', NULL, 'selector_simple', 3),

-- Sección: Autoevaluación de Habilidades
(124, 1, 14, 'Compártenos con tus palabras ¿Qué entiendes por power skills/soft skills?', NULL, 'texto_largo', 1);

-- --- Opciones de Pregunta ---
INSERT INTO opciones_pregunta (id, pregunta_id, texto_opcion, valor_opcion, orden) VALUES
-- Opciones para Género (pregunta_id = 104)
(501, 104, 'Femenino', 'femenino', 1), (502, 104, 'Masculino', 'masculino', 2), (503, 104, 'No binario', 'no_binario', 3), (504, 104, 'Prefiero no especificar', 'no_especificado', 4),
-- Opciones para Rango de edad (pregunta_id = 105)
(505, 105, '20 - 24', '20-24', 1), (506, 105, '25 - 29', '25-29', 2), (507, 105, '30 - 34', '30-34', 3), (508, 105, '35 - 39', '35-39', 4), (509, 105, '40 - 44', '40-44', 5), (510, 105, '45 - 49', '45-49', 6),
-- Opciones para Ultimo grado de estudios (pregunta_id = 106)
(511, 106, 'Medio superior', 'medio_superior', 1), (512, 106, 'Profesional técnico terminal', 'profesional_tecnico', 2), (513, 106, 'Superior', 'superior', 3), (514, 106, 'Técnico superior universitario y profesional asociado', 'tecnico_superior', 4), (515, 106, 'Especialización', 'especializacion', 5), (516, 106, 'Maestría', 'maestria', 6), (517, 106, 'Doctorado', 'doctorado', 7),
-- Opciones para Área de estudio (pregunta_id = 108)
(518, 108, 'Educación', 'educacion', 1), (519, 108, 'Artes y humanidades', 'artes_humanidades', 2), (520, 108, 'Ciencias Sociales, Administración y Derecho', 'sociales_admin_derecho', 3), (521, 108, 'Ciencias Naturales o Exactas', 'ciencias_naturales_exactas', 4), (522, 108, 'Ciencias de la Computación', 'computacion', 5), (523, 108, 'Ingeniería, Manufactura y Construcción', 'ingenieria', 6), (524, 108, 'Agronomía y veterinaria', 'agronomia_veterinaria', 7), (525, 108, 'Salud', 'salud', 8), (526, 108, 'Servicios', 'servicios', 9), (527, 108, 'Otra', 'otra', 10),
-- Opciones para Tecnologías (pregunta_id = 109)
(528, 109, 'Java', 'java', 1), (529, 109, 'Python', 'python', 2), (530, 109, 'JavaScript', 'javascript', 3), (531, 109, 'C++', 'cpp', 4), (532, 109, 'Swift', 'swift', 5), (533, 109, 'TypeScript', 'typescript', 6), (534, 109, 'PHP', 'php', 7), (535, 109, 'C#', 'csharp', 8), (536, 109, 'Otra', 'otra', 9),
-- Opciones para ¿Trabajando? (pregunta_id = 110)
(537, 110, 'Sí', 'si', 1), (538, 110, 'No', 'no', 2),
-- Opciones para Horas de trabajo (pregunta_id = 111)
(539, 111, 'Tiempo completo: 40 a más horas', 'tiempo_completo', 1), (540, 111, 'Medio tiempo: 20 horas o menos', 'medio_tiempo', 2), (541, 111, 'Por proyecto: horas variables', 'por_proyecto', 3),
-- Opciones para Modalidad de trabajo (pregunta_id = 112)
(542, 112, 'Remoto', 'remoto', 1), (543, 112, 'Presencial', 'presencial', 2), (544, 112, 'Híbrido', 'hibrido', 3), (545, 112, 'Otro', 'otro', 4),
-- Opciones para Posición actual (pregunta_id = 113)
(546, 113, 'Desarrollador frontend/Backend/Aplicaciones móviles/FullStack', 'desarrollador', 1), (547, 113, 'Ingeniera Sofware/Cibrseguridad', 'ingenieria_seguridad', 2), (548, 113, 'Analista de data/Administradora de bases de datos', 'datos', 3), (549, 113, 'Diseñadora UX/UI', 'ux_ui', 4), (550, 113, 'Lider de equipo/Team leader', 'lider_equipo', 5), (551, 113, 'Project manager', 'project_manager', 6), (552, 113, 'Scrum master', 'scrum_master', 7),
-- Opciones para Rango salarial (pregunta_id = 114)
(553, 114, '13,000 - 17,000', '13k_17k', 1), (554, 114, '18,000 - 22,000', '18k_22k', 2), (555, 114, '23,000 - 27,000', '23k_27k', 3), (556, 114, '28,000 - 32,000', '28k_32k', 4),
-- Opciones para Razón para moverse (pregunta_id = 115)
(557, 115, 'Busco mayor salario y/o beneficios', 'mayor_salario', 1), (558, 115, 'Desarrollo de habilidades técnicas', 'desarrollo_tecnico', 2), (559, 115, 'Falta de reconocimiento en mi actual empleo', 'falta_reconocimiento', 3), (560, 115, 'Búsqueda de mayor flexibilidad', 'mayor_flexibilidad', 4), (561, 115, 'Necesidad de un mejor equilibrio entre la vida laboral y personal.', 'equilibrio_vida_laboral', 5), (562, 115, 'Otro', 'otro', 6),
-- Opciones para Expectativa salarial (pregunta_id = 116)
(563, 116, 'Entre 3 a 5 mil', '3k_5k', 1), (564, 116, 'Entre 6 a 10 mil', '6k_10k', 2), (565, 116, 'De 11 a 17 mil', '11k_17k', 3),
-- Opciones para ¿Cursos completados? (pregunta_id = 117)
(566, 117, 'Sí', 'si', 1), (567, 117, 'No', 'no', 2),
-- Opciones para ¿Proyectos construidos? (pregunta_id = 118)
(568, 118, 'Entre 1 a 5', '1_5', 1), (569, 118, 'Entre 6 a 10', '6_10', 2), (570, 118, 'Entre 10 a 14', '10_14', 3), (571, 118, 'Más de 15', 'mas_15', 4),
-- Opciones para ¿Participa en cuidado? (pregunta_id = 121)
(572, 121, 'Sí', 'si', 1), (573, 121, 'No', 'no', 2),
-- Opciones para Tiempo de cuidado (pregunta_id = 122)
(574, 122, '40 horas a la semana', '40_horas', 1), (575, 122, '20 horas a la semana', '20_horas', 2), (576, 122, '10 horas a la semana', '10_horas', 3),
-- Opciones para Incremento de horas por cuidado (pregunta_id = 123)
(577, 123, 'Al menos entre 10 - 15 horas a la semana', '10_15_horas', 1), (578, 123, 'Al menos entre 20 - 30 horas a la semana', '20_30_horas', 2);

-- ========= SECCIÓN 4: ACTUALIZACIÓN DE SECUENCIAS =========

-- Actualizar las secuencias para que los próximos IDs insertados no colisionen
-- Esto es importante porque hemos insertado IDs manualmente
SELECT setval('cuestionarios_id_seq', (SELECT MAX(id) FROM cuestionarios));
SELECT setval('cuestionario_secciones_id_seq', (SELECT MAX(id) FROM cuestionario_secciones));
SELECT setval('preguntas_id_seq', (SELECT MAX(id) FROM preguntas));
SELECT setval('opciones_pregunta_id_seq', (SELECT MAX(id) FROM opciones_pregunta));


ALTER TABLE user_profiles ADD COLUMN age_range VARCHAR(50);

ALTER TABLE user_profiles ADD COLUMN name VARCHAR(100);

ALTER TABLE user_profiles ADD COLUMN work_hours_per_week VARCHAR(50);

ALTER TABLE user_profiles ADD COLUMN caregiver_status VARCHAR(50);

-- Fin del script
