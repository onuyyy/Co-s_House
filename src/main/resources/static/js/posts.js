// 게시글 목록 페이지 JavaScript

// 필터 이름 매핑
const filterNames = {
    housingType: '주거형태',
    roomCount: '방개수',
    familyType: '가족형태',
    familyCount: '가족구성원',
    projectType: '작업분야'
};

document.addEventListener('DOMContentLoaded', function() {
    // 필터 기능
    const filterBtns = document.querySelectorAll('.filter-btn');
    const filterDropdowns = document.querySelectorAll('.filter-dropdown');

    filterBtns.forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.stopPropagation();

            const filterType = this.getAttribute('data-filter');
            const dropdown = document.getElementById(filterType + 'Dropdown');
            const isActive = this.classList.contains('active');

            // 모든 필터 드롭다운 닫기
            filterBtns.forEach(b => b.classList.remove('active'));
            filterDropdowns.forEach(d => d.classList.remove('show'));

            if (!isActive) {
                this.classList.add('active');
                dropdown.classList.add('show');
            }
        });
    });

    // 필터 옵션 선택
    const radioInputs = document.querySelectorAll('.filter-dropdown input[type="radio"]');
    radioInputs.forEach(input => {
        input.addEventListener('change', function() {
            const form = document.getElementById('searchForm');
            form.submit();
        });
    });

    // 외부 클릭 시 드롭다운 닫기
    document.addEventListener('click', function() {
        filterBtns.forEach(btn => btn.classList.remove('active'));
        filterDropdowns.forEach(dropdown => dropdown.classList.remove('show'));
    });

    // 드롭다운 내부 클릭 시 이벤트 전파 중단
    filterDropdowns.forEach(dropdown => {
        dropdown.addEventListener('click', function(e) {
            e.stopPropagation();
        });
    });

    // 페이지 로드 시 선택된 필터 표시
    updateSelectedFilters();
});

// 선택된 필터 표시 업데이트
function updateSelectedFilters() {
    const selectedFiltersContainer = document.getElementById('selectedFilters');
    const filterTagsContainer = selectedFiltersContainer.querySelector('.filter-tags');
    filterTagsContainer.innerHTML = '';

    let hasSelectedFilters = false;

    // 모든 라디오 버튼 체크
    const radioInputs = document.querySelectorAll('.filter-dropdown input[type="radio"]:checked');
    radioInputs.forEach(input => {
        const filterName = input.getAttribute('name');
        const filterValue = input.value;

        // 값이 있고 '전체'가 아닌 경우에만 표시
        if (filterValue && filterValue !== '') {
            hasSelectedFilters = true;
            const displayText = input.nextElementSibling ? input.nextElementSibling.textContent : filterValue;

            const tag = document.createElement('span');
            tag.className = 'filter-tag';
            tag.innerHTML = `
                ${filterNames[filterName]}: ${displayText}
                <button type="button" onclick="removeFilter('${filterName}')" class="remove-filter">×</button>
            `;
            filterTagsContainer.appendChild(tag);
        }
    });

    // 선택된 필터가 있으면 컨테이너 표시
    selectedFiltersContainer.style.display = hasSelectedFilters ? 'flex' : 'none';
}

// 특정 필터 제거
function removeFilter(filterName) {
    const form = document.getElementById('searchForm');
    const input = form.querySelector(`input[name="${filterName}"][value=""]`);
    if (input) {
        input.checked = true;
        form.submit();
    }
}

// 모든 필터 초기화
function clearAllFilters() {
    const form = document.getElementById('searchForm');
    const allEmptyInputs = form.querySelectorAll('input[type="radio"][value=""]');
    allEmptyInputs.forEach(input => {
        input.checked = true;
    });
    form.submit();
}

// 북마크 토글 함수
function toggleBookmark(button) {
    const postId = button.getAttribute('data-post-id');

    // 로그인 확인
    const currentUserId = document.querySelector('body').getAttribute('data-user-id');
    if (!currentUserId) {
        if (confirm('로그인이 필요한 서비스입니다. 로그인 페이지로 이동하시겠습니까?')) {
            window.location.href = '/login';
        }
        return;
    }

    // AJAX 요청
    fetch(`/posts/${postId}/scrap`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('스크랩 처리 중 오류가 발생했습니다.');
        }
        return response.json();
    })
    .then(data => {

        // 버튼 상태 업데이트 (서버에서 scraped로 응답)
        if (data.scraped) {
            button.classList.add('active');
        } else {
            button.classList.remove('active');
        }

        // 스크랩 수 업데이트 - 클래스명으로 직접 찾기
        const postCard = button.closest('.post-card');

        const scrapCountElement = postCard.querySelector('.scrap-count');
        if (scrapCountElement) {
            console.log('Updating scrap count from', scrapCountElement.textContent, 'to', data.scrapCount);
            scrapCountElement.textContent = data.scrapCount;
        } else {
            console.error('Scrap count element not found!');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('스크랩 처리 중 오류가 발생했습니다.');
    });
}
