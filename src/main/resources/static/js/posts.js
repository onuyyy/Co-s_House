// 게시글 목록 페이지 JavaScript

document.addEventListener('DOMContentLoaded', function() {
    console.log('Posts page loaded');

    // 필터 드롭다운 토글
    const filterBtns = document.querySelectorAll('.filter-btn');
    const filterDropdowns = document.querySelectorAll('.filter-dropdown');

    filterBtns.forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.stopPropagation();
            const filterType = this.getAttribute('data-filter');
            const dropdown = document.getElementById(filterType + 'Dropdown');
            
            // 다른 드롭다운 닫기
            filterDropdowns.forEach(dd => {
                if (dd !== dropdown) {
                    dd.classList.remove('show');
                }
            });
            filterBtns.forEach(b => {
                if (b !== this) {
                    b.classList.remove('active');
                }
            });

            // 현재 드롭다운 토글
            dropdown.classList.toggle('show');
            this.classList.toggle('active');
        });
    });

    // 필터 선택 시 폼 제출
    const searchForm = document.getElementById('searchForm');
    const filterInputs = searchForm.querySelectorAll('input[type="radio"]');

    filterInputs.forEach(input => {
        input.addEventListener('change', function() {
            
            // 선택된 필터의 드롭다운 닫기
            const dropdown = this.closest('.filter-dropdown');
            if (dropdown) {
                dropdown.classList.remove('show');
            }

            // 필터 버튼 상태 업데이트
            const filterGroup = this.closest('.filter-group');
            const filterBtn = filterGroup.querySelector('.filter-btn');
            
            if (this.value === '') {
                filterBtn.classList.remove('active');
            } else {
                filterBtn.classList.add('active');
            }

            searchForm.submit();
        });
    });

    // 드롭다운 외부 클릭 시 닫기
    document.addEventListener('click', function(e) {
        if (!e.target.closest('.filter-group')) {
            filterDropdowns.forEach(dropdown => {
                dropdown.classList.remove('show');
            });
            filterBtns.forEach(btn => {
                btn.classList.remove('active');
            });
        }
    });

    // 북마크 토글
    window.toggleBookmark = function(btn) {
        btn.classList.toggle('active');
        // TODO: 서버에 북마크 상태 저장
    };

    // URL 파라미터로 필터 상태 복원
    const urlParams = new URLSearchParams(window.location.search);
    
    filterInputs.forEach(input => {
        const paramValue = urlParams.get(input.name);
        if (paramValue !== null) {
            if (input.value === paramValue) {
                input.checked = true;
                const filterGroup = input.closest('.filter-group');
                const filterBtn = filterGroup.querySelector('.filter-btn');
                if (paramValue !== '') {
                    filterBtn.classList.add('active');
                }
            }
        } else if (input.value === '') {
            // 파라미터가 없으면 "전체" 선택
            input.checked = true;
        }
    });
});