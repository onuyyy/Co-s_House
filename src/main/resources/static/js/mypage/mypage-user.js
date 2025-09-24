/*<![CDATA[*/
// 페이지 로드 시 주소 분리
document.addEventListener('DOMContentLoaded', function() {
    var fullAddress = document.getElementById('userAddress').value;
    if (fullAddress) {
        // 일반적인 상세주소 패턴들 (동/호수, 층, 호, 빌딩명 등)
        var detailPatterns = [
            /\s+\d+동\s*\d+호.*$/,      // 101동 202호
            /\s+\d+층\s*.*$/,           // 11층 패스트캠퍼스
            /\s+\d+호.*$/,              // 202호
            /\s+\d+번지.*$/,            // 123번지
            /\s+[가-힣A-Za-z0-9]+빌딩.*$/, // 삼성빌딩
            /\s+[가-힣A-Za-z0-9]+아파트.*$/ // 래미안아파트
        ];

        var baseAddr = fullAddress;
        var detailAddr = '';

        for (var i = 0; i < detailPatterns.length; i++) {
            var match = fullAddress.match(detailPatterns[i]);
            if (match) {
                baseAddr = fullAddress.substring(0, match.index);
                detailAddr = match[0].trim();
                break;
            }
        }

        document.getElementById('userAddress').value = baseAddr;
        document.getElementById('userDetailAddress').value = detailAddr;
    }
});

// 전화번호 포맷팅
document.getElementById('userPhone').addEventListener('input', function(e) {
    let value = e.target.value.replace(/[^0-9]/g, '');

    // 11자리까지만 허용 (010-1234-5678)
    if (value.length > 11) {
        value = value.substring(0, 11);
    }

    if (value.length >= 3) {
        if (value.length >= 7) {
            value = value.replace(/(\d{3})(\d{4})(\d{4})/, '$1-$2-$3');
        } else {
            value = value.replace(/(\d{3})(\d{0,4})/, '$1-$2');
        }
    }
    e.target.value = value;
});

// 다음 우편번호 API를 이용한 주소 검색
document.querySelector('.address-search-btn').addEventListener('click', function() {
    new daum.Postcode({
        oncomplete: function(data) {
            // 사용자가 선택한 주소 정보를 받아서 처리
            var addr = ''; // 주소 변수
            var extraAddr = ''; // 참고항목 변수

            // 사용자가 선택한 주소 타입에 따라 해당 주소 값을 가져온다.
            if (data.userSelectedType === 'R') { // 사용자가 도로명 주소를 선택했을 경우
                addr = data.roadAddress;
            } else { // 사용자가 지번 주소를 선택했을 경우(J)
                addr = data.jibunAddress;
            }

            // 주소 정보를 해당 필드에 넣는다 (우편번호, 참고항목 제외)
            document.getElementById('userAddress').value = addr;

            // 커서를 상세주소 필드로 이동한다.
            document.getElementById('userDetailAddress').focus();
        }
    }).open();
});

// 회원탈퇴 비밀번호 검증
document.addEventListener('DOMContentLoaded', function() {
    const withdrawalPasswordField = document.getElementById('withdrawalPassword');

    if (withdrawalPasswordField) {
        withdrawalPasswordField.addEventListener('input', function(e) {
            const withdrawalPassword = e.target.value;

            validatePassword(withdrawalPassword, 'withdrawalPasswordFeedback',
                function() { // onValid
                    isWithdrawalPasswordValid = true;
                    updateWithdrawalFieldColors();
                    checkWithdrawalConditions();
                },
                function() { // onInvalid
                    isWithdrawalPasswordValid = false;
                    updateWithdrawalFieldColors();
                    checkWithdrawalConditions();
                }
            );
        });
    }
});

// 탈퇴 폼 필드 색상 관리
function updateWithdrawalFieldColors() {
    const reasonField = document.getElementById('withdrawalReason');
    const passwordField = document.getElementById('withdrawalPassword');

    // 탈퇴 사유 필드
    if (reasonField && reasonField.value.trim() !== '') {
        reasonField.classList.add('filled');
    } else if (reasonField) {
        reasonField.classList.remove('filled');
    }

    // 비밀번호 필드 (소셜 로그인이 아닌 경우만)
    if (passwordField) {
        if (passwordField.value.trim() !== '') {
            passwordField.classList.add('filled');
        } else {
            passwordField.classList.remove('filled');
        }
    }
}

// 탈퇴 사유 변경 이벤트
document.addEventListener('DOMContentLoaded', function() {
    const reasonField = document.getElementById('withdrawalReason');
    const passwordField = document.getElementById('withdrawalPassword');

    if (reasonField) {
        reasonField.addEventListener('change', updateWithdrawalFieldColors);
    }

    if (passwordField) {
        passwordField.addEventListener('input', updateWithdrawalFieldColors);
    }

    // 초기 색상 설정
    updateWithdrawalFieldColors();
});

// 비밀번호 검증 상태 추적 변수
let isWithdrawalPasswordValid = false;

// 탈퇴 조건 확인 함수
function checkWithdrawalConditions() {
    const reasonField = document.getElementById('withdrawalReason');
    const passwordField = document.getElementById('withdrawalPassword');
    const agreeCheckbox = document.getElementById('withdrawalAgree');
    const withdrawalBtn = document.querySelector('.withdrawal-btn');

    let isValid = true;

    // 탈퇴 사유 확인
    if (!reasonField || reasonField.value.trim() === '') {
        isValid = false;
    }

    // 비밀번호 확인 (소셜 로그인이 아닌 경우)
    if (passwordField) {
        if (passwordField.value.trim() === '') {
            isValid = false;
        } else if (!isWithdrawalPasswordValid) {
            // 비밀번호가 입력되었지만 검증에 실패한 경우
            isValid = false;
        }
    }

    // 동의 체크박스 확인
    if (!agreeCheckbox.checked) {
        isValid = false;
    }

    withdrawalBtn.disabled = !isValid;
}

// 탈퇴 조건 이벤트 리스너
document.addEventListener('DOMContentLoaded', function() {
    const reasonField = document.getElementById('withdrawalReason');
    const passwordField = document.getElementById('withdrawalPassword');
    const agreeCheckbox = document.getElementById('withdrawalAgree');

    if (reasonField) {
        reasonField.addEventListener('change', checkWithdrawalConditions);
    }

    if (passwordField) {
        passwordField.addEventListener('input', checkWithdrawalConditions);
    }

    if (agreeCheckbox) {
        agreeCheckbox.addEventListener('change', checkWithdrawalConditions);
    }

    // 초기 상태 확인
    checkWithdrawalConditions();
});

// 회원탈퇴 섹션 토글
function toggleWithdrawal() {
    const content = document.getElementById('withdrawalContent');
    const arrow = document.querySelector('.withdrawal-arrow');

    if (content.style.maxHeight) {
        // 닫기
        content.style.maxHeight = null;
        arrow.classList.remove('rotated');
    } else {
        // 열기
        content.style.maxHeight = content.scrollHeight + "px";
        arrow.classList.add('rotated');
    }
}

// 페이지 로드 시 회원탈퇴 섹션 닫기
document.addEventListener('DOMContentLoaded', function() {
    const content = document.getElementById('withdrawalContent');
    content.style.maxHeight = '0';
    content.style.overflow = 'hidden';
});

// 공통 비밀번호 검증 함수
function validatePassword(password, feedbackElementId, onValid, onInvalid) {
    const feedback = document.getElementById(feedbackElementId);

    if (password.trim() === '') {
        feedback.innerHTML = '';
        if (onInvalid) onInvalid();
        return;
    }

    // 서버에 비밀번호 검증 요청
    fetch('/mypage/validatePassword', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: 'currentPassword=' + encodeURIComponent(password)
    })
    .then(response => response.json())
    .then(isValid => {
        if (isValid) {
            feedback.innerHTML = '<span style="color: green;"><i class="fas fa-check"></i> 비밀번호가 확인되었습니다.</span>';
            if (onValid) onValid();
        } else {
            feedback.innerHTML = '<span style="color: red;"><i class="fas fa-times"></i> 비밀번호가 일치하지 않습니다.</span>';
            if (onInvalid) onInvalid();
        }
    })
    .catch(error => {
        console.error('비밀번호 검증 중 오류:', error);
        feedback.innerHTML = '<span style="color: red;"><i class="fas fa-exclamation-triangle"></i> 검증 중 오류가 발생했습니다.</span>';
        if (onInvalid) onInvalid();
    });
}

// 현재 비밀번호 검증 (비밀번호 변경용)
document.getElementById('currentPassword').addEventListener('input', function(e) {
    const currentPassword = e.target.value;
    const newPasswordField = document.getElementById('newPassword');
    const confirmPasswordField = document.getElementById('confirmPassword');
    const currentPasswordField = e.target;

    validatePassword(currentPassword, 'currentPasswordFeedback',
        function() { // onValid
            newPasswordField.disabled = false;
            confirmPasswordField.disabled = false;
            currentPasswordField.classList.add('filled');
            // 활성화되면서 빨간 테두리 적용
            newPasswordField.classList.remove('filled');
            confirmPasswordField.classList.remove('filled');
        },
        function() { // onInvalid
            newPasswordField.disabled = true;
            confirmPasswordField.disabled = true;
            newPasswordField.value = '';
            confirmPasswordField.value = '';
            newPasswordField.classList.remove('filled');
            confirmPasswordField.classList.remove('filled');
            if (currentPassword.trim() !== '') {
                currentPasswordField.classList.remove('filled');
            } else {
                currentPasswordField.classList.remove('filled');
            }
        }
    );
});

// 새 비밀번호 강도 검증
document.getElementById('newPassword').addEventListener('input', function(e) {
    const newPassword = e.target.value;
    const feedback = document.getElementById('newPasswordFeedback');
    const newPasswordField = e.target;

    if (newPassword === '') {
        feedback.innerHTML = '';
        newPasswordField.classList.remove('filled');
        return;
    }

    // 현재 비밀번호와 평문이 같은지 기본 체크 (완전하지 않지만 UX 향상)
    const currentPassword = document.getElementById('currentPassword').value;
    if (currentPassword && newPassword === currentPassword) {
        feedback.innerHTML = '<span style="color: red;"><i class="fas fa-times"></i> 새 비밀번호는 현재 비밀번호와 달라야 합니다.</span>';
        newPasswordField.classList.remove('filled');
        return;
    }
    // 주의: 이 검증은 평문 비교이므로 완전하지 않음. 최종 검증은 서버에서 수행

    // 비밀번호 강도 검증
    const hasLength = newPassword.length >= 8;
    const hasUpper = /[A-Z]/.test(newPassword);
    const hasLower = /[a-z]/.test(newPassword);
    const hasNumber = /\d/.test(newPassword);
    const hasSpecial = /[!@#$%^&*(),.?":{}|<>]/.test(newPassword);

    let strength = 0;
    let messages = [];

    if (hasLength) strength++;
    else messages.push('8자 이상');

    if (hasUpper && hasLower) strength++;
    else messages.push('영문 대소문자');

    if (hasNumber) strength++;
    else messages.push('숫자');

    if (hasSpecial) strength++;
    else messages.push('특수문자');

    if (strength === 4) {
        feedback.innerHTML = '<span style="color: green;"><i class="fas fa-check"></i> 강력한 비밀번호입니다.</span>';
        newPasswordField.classList.add('filled');
    } else if (strength >= 3) {
        feedback.innerHTML = '<span style="color: orange;"><i class="fas fa-exclamation-triangle"></i> 보통 강도: ' + messages.join(', ') + ' 필요</span>';
        newPasswordField.classList.remove('filled');
    } else {
        feedback.innerHTML = '<span style="color: red;"><i class="fas fa-times"></i> 약한 비밀번호: ' + messages.join(', ') + ' 필요</span>';
        newPasswordField.classList.remove('filled');
    }

    // 비밀번호 확인 필드도 다시 검증
    validatePasswordMatch();
});

// 비밀번호 확인 검증
document.getElementById('confirmPassword').addEventListener('input', validatePasswordMatch);

function validatePasswordMatch() {
    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    const feedback = document.getElementById('confirmPasswordFeedback');
    const confirmPasswordField = document.getElementById('confirmPassword');

    if (confirmPassword === '') {
        feedback.innerHTML = '';
        confirmPasswordField.classList.remove('filled');
        return;
    }

    if (newPassword === confirmPassword) {
        feedback.innerHTML = '<span style="color: green;"><i class="fas fa-check"></i> 비밀번호가 일치합니다.</span>';
        confirmPasswordField.classList.add('filled');
    } else {
        feedback.innerHTML = '<span style="color: red;"><i class="fas fa-times"></i> 비밀번호가 일치하지 않습니다.</span>';
        confirmPasswordField.classList.remove('filled');
    }
}

// 회원탈퇴 처리 함수
function handleWithdrawal() {
    if (!confirm('정말로 회원탈퇴를 하시겠습니까?\n\n탈퇴 후에는 모든 데이터가 삭제되며 복구할 수 없습니다.')) {
        return;
    }

    if (!confirm('마지막 확인입니다.\n\n회원탈퇴를 진행하시겠습니까?')) {
        return;
    }

    // 별도의 form을 생성하여 탈퇴 요청
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = '/mypage/mypageUserDelete';

    // CSRF 토큰이 있다면 추가
    const csrfToken = document.querySelector('meta[name="_csrf"]');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]');

    if (csrfToken && csrfHeader) {
        const csrfInput = document.createElement('input');
        csrfInput.type = 'hidden';
        csrfInput.name = '_csrf';
        csrfInput.value = csrfToken.getAttribute('content');
        form.appendChild(csrfInput);
    }

    document.body.appendChild(form);
    form.submit();
}

// 폼 제출 확인
document.querySelector('.user-update-form').addEventListener('submit', function(e) {
    // 기본 주소와 상세주소를 합치기
    var baseAddress = document.getElementById('userAddress').value;
    var detailAddress = document.getElementById('userDetailAddress').value;

    if (baseAddress && detailAddress) {
        document.getElementById('userAddress').value = baseAddress + ' ' + detailAddress;
    }

    if (!confirm('정보를 수정하시겠습니까?')) {
        e.preventDefault();
    }
});
/*]]>*/