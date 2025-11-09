-- 문서 샘플 데이터 삽입
-- 샘플 문서 1: 출장여비지급규정
INSERT INTO documents (file_name, regulation_type, upload_time, content, uploader_id, file_size, status)
VALUES (
    '출장여비지급규정_2024.pdf',
    '출장여비지급규정',
    TIMESTAMPADD(DAY, -5, CURRENT_TIMESTAMP),
    '제1조 (목적)
이 규정은 회사의 임직원이 업무상 출장 시 지급하는 여비 및 경비에 관한 사항을 규정함을 목적으로 한다.

제2조 (적용범위)
이 규정은 회사의 모든 임직원에게 적용한다.

제3조 (출장여비의 종류)
① 출장여비는 다음 각 호와 같다.
  1. 교통비
  2. 숙박비
  3. 식비
  4. 기타 경비

제4조 (교통비)
① 교통비는 실제 사용한 교통수단의 요금을 기준으로 지급한다.
② 항공권은 일반석 기준으로 지급하며, 비즈니스석 이상은 사전 승인이 필요하다.

제5조 (숙박비)
① 숙박비는 1일 1인당 15만원을 한도로 지급한다.
② 서울, 부산 등 대도시는 1일 1인당 20만원을 한도로 지급할 수 있다.

제6조 (식비)
① 식비는 1일 1인당 5만원을 한도로 지급한다.
② 조식, 중식, 석식을 각각 1만원, 2만원, 2만원으로 구분하여 지급할 수 있다.',
    (SELECT id FROM users WHERE username = 'admin' LIMIT 1),
    15234,
    'indexed'
);

-- 샘플 문서 2: 접대비사용규정
INSERT INTO documents (file_name, regulation_type, upload_time, content, uploader_id, file_size, status)
VALUES (
    '접대비사용규정_2024.docx',
    '접대비사용규정',
    TIMESTAMPADD(DAY, -3, CURRENT_TIMESTAMP),
    '제1조 (목적)
이 규정은 회사의 접대비 사용 및 관리에 관한 사항을 규정함을 목적으로 한다.

제2조 (정의)
① "접대비"란 업무상 필요에 따라 외부인을 접대하는데 소요되는 비용을 말한다.
② 접대비에는 식대, 선물비, 기타 접대에 소요되는 비용이 포함된다.

제3조 (사용한도)
① 접대비는 월 1인당 50만원을 한도로 사용할 수 있다.
② 부서별로는 월 500만원을 한도로 사용할 수 있다.

제4조 (사용절차)
① 접대비 사용 시 사전에 부서장의 승인을 받아야 한다.
② 접대비 사용 후 7일 이내에 영수증과 함께 정산하여야 한다.

제5조 (사용금지)
다음 각 호의 경우 접대비 사용을 금지한다.
  1. 개인적인 목적의 접대
  2. 법령에 위반되는 접대
  3. 회사 이미지에 손상을 줄 수 있는 접대',
    (SELECT id FROM users WHERE username = 'admin' LIMIT 1),
    18765,
    'indexed'
);

-- 샘플 문서 3: 윤리규정
INSERT INTO documents (file_name, regulation_type, upload_time, content, uploader_id, file_size, status)
VALUES (
    '윤리규정_2024.txt',
    '윤리규정',
    TIMESTAMPADD(DAY, -1, CURRENT_TIMESTAMP),
    '제1장 총칙

제1조 (목적)
이 규정은 회사의 임직원이 지켜야 할 윤리 기준과 행동 강령을 규정하여 건전한 기업 문화를 조성하고 사회적 책임을 다함을 목적으로 한다.

제2조 (적용범위)
이 규정은 회사의 모든 임직원(임원, 직원, 파견근로자 등 포함)에게 적용한다.

제2장 기본 윤리

제3조 (정직과 신뢰)
① 임직원은 모든 업무를 정직하고 투명하게 수행하여야 한다.
② 거짓 정보를 제공하거나 허위 보고를 하여서는 아니 된다.

제4조 (공정한 업무 수행)
① 임직원은 공정하고 객관적인 기준에 따라 업무를 수행하여야 한다.
② 개인적 이해관계와 업무를 혼동하여서는 아니 된다.

제5조 (비밀 유지)
① 임직원은 업무상 알게 된 회사의 기밀 정보를 외부에 누설하여서는 아니 된다.
② 퇴직 후에도 기밀 유지 의무는 계속된다.

제3장 금지 행위

제6조 (부정 수수)
① 임직원은 업무와 관련하여 금전, 물품, 향응 등을 수수하여서는 아니 된다.
② 예외적으로 소액의 기념품 등은 회사가 정한 기준에 따라 허용될 수 있다.

제7조 (이해충돌)
① 임직원은 개인적 이해관계가 업무 수행에 영향을 미칠 수 있는 경우 이를 신고하여야 한다.
② 이해충돌이 발생한 경우 해당 업무에서 제외되어야 한다.',
    (SELECT id FROM users WHERE username = 'admin' LIMIT 1),
    23456,
    'indexed'
);

-- 샘플 문서 4: 경비지급규정
INSERT INTO documents (file_name, regulation_type, upload_time, content, uploader_id, file_size, status)
VALUES (
    '경비지급규정_2024.pdf',
    '경비지급규정',
    TIMESTAMPADD(HOUR, -2, CURRENT_TIMESTAMP),
    '제1조 (목적)
이 규정은 회사의 각종 경비 지급에 관한 사항을 규정함을 목적으로 한다.

제2조 (경비의 종류)
① 일반경비: 사무용품 구입비, 통신비, 교통비 등
② 특별경비: 행사비, 교육훈련비, 연구개발비 등

제3조 (지급 절차)
① 경비 지급은 사전 승인을 원칙으로 한다.
② 긴급한 경우 사후 승인을 받을 수 있다.

제4조 (지급 한도)
① 일반경비는 월 100만원을 한도로 지급한다.
② 특별경비는 사전 승인 시 별도 한도를 적용한다.

제5조 (정산)
① 경비 사용 후 10일 이내에 정산하여야 한다.
② 영수증 등 증빙서류를 제출하여야 한다.',
    (SELECT id FROM users WHERE username = 'admin' LIMIT 1),
    12345,
    'indexed'
);

-- 샘플 문서 5: 문서관리규정
INSERT INTO documents (file_name, regulation_type, upload_time, content, uploader_id, file_size, status)
VALUES (
    '문서관리규정_2024.docx',
    '문서관리규정',
    TIMESTAMPADD(HOUR, -1, CURRENT_TIMESTAMP),
    '제1조 (목적)
이 규정은 회사의 문서 작성, 보관, 관리에 관한 사항을 규정함을 목적으로 한다.

제2조 (문서의 분류)
① 일반문서: 일반적인 업무 문서
② 중요문서: 계약서, 인허가서 등
③ 기밀문서: 기밀에 해당하는 문서

제3조 (문서 작성)
① 문서는 명확하고 간결하게 작성하여야 한다.
② 문서에는 작성일자, 작성자, 승인자 등을 명시하여야 한다.

제4조 (문서 보관)
① 일반문서는 3년간 보관한다.
② 중요문서는 10년간 보관한다.
③ 기밀문서는 별도 보관소에 보관한다.

제5조 (문서 폐기)
① 보관기간이 경과한 문서는 폐기할 수 있다.
② 중요문서의 폐기는 사전 승인이 필요하다.',
    (SELECT id FROM users WHERE username = 'admin' LIMIT 1),
    15678,
    'indexed'
);
