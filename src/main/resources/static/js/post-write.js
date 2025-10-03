// 포스트 작성 페이지 JavaScript

document.addEventListener('DOMContentLoaded', function() {
    // 가이드 박스 토글
    const guideToggle = document.querySelector('.guide-toggle');
    const guideContent = document.querySelector('.guide-content');
    
    if (guideToggle && guideContent) {
        guideToggle.addEventListener('click', function() {
            if (guideContent.style.display === 'none') {
                guideContent.style.display = 'block';
                guideToggle.textContent = '▲';
            } else {
                guideContent.style.display = 'none';
                guideToggle.textContent = '▼';
            }
        });
    }

    // 필수 정보 박스 토글
    const requiredToggle = document.querySelector('.required-toggle');
    const requiredContent = document.querySelector('.required-info-content');
    
    if (requiredToggle && requiredContent) {
        requiredToggle.addEventListener('click', function() {
            if (requiredContent.style.display === 'none') {
                requiredContent.style.display = 'grid';
                requiredToggle.textContent = '▲';
            } else {
                requiredContent.style.display = 'none';
                requiredToggle.textContent = '▼';
            }
        });
    }

    // 이미지 업로드 처리
    const imageUpload = document.getElementById('imageUpload');
    const imagePreview = document.getElementById('imagePreview');
    let uploadedFiles = [];

    if (imageUpload) {
        imageUpload.addEventListener('change', function(e) {
            const files = Array.from(e.target.files);
            
            files.forEach(file => {
                // 파일 크기 체크 (20MB)
                if (file.size > 20 * 1024 * 1024) {
                    alert('파일 크기는 20MB를 초과할 수 없습니다: ' + file.name);
                    return;
                }

                // 파일 형식 체크
                const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp', 'image/heif', 'image/heic', 'image/gif'];
                if (!allowedTypes.includes(file.type)) {
                    alert('지원하지 않는 파일 형식입니다: ' + file.name);
                    return;
                }

                uploadedFiles.push(file);
                displayImage(file);
            });
        });
    }

    function displayImage(file) {
        const reader = new FileReader();
        
        reader.onload = function(e) {
            const div = document.createElement('div');
            div.className = 'image-preview-item';
            
            const img = document.createElement('img');
            img.src = e.target.result;
            
            const removeBtn = document.createElement('button');
            removeBtn.className = 'image-preview-remove';
            removeBtn.innerHTML = '×';
            removeBtn.type = 'button';
            removeBtn.addEventListener('click', function() {
                const index = uploadedFiles.indexOf(file);
                if (index > -1) {
                    uploadedFiles.splice(index, 1);
                }
                div.remove();
            });
            
            div.appendChild(img);
            div.appendChild(removeBtn);
            imagePreview.appendChild(div);
        };
        
        reader.readAsDataURL(file);
    }

    // 드래그 앤 드롭 처리
    const uploadBox = document.querySelector('.image-upload-box');
    
    if (uploadBox) {
        uploadBox.addEventListener('dragover', function(e) {
            e.preventDefault();
            uploadBox.style.borderColor = '#35c5f0';
            uploadBox.style.backgroundColor = '#f0f9ff';
        });

        uploadBox.addEventListener('dragleave', function(e) {
            e.preventDefault();
            uploadBox.style.borderColor = '#e0e0e0';
            uploadBox.style.backgroundColor = '#fafafa';
        });

        uploadBox.addEventListener('drop', function(e) {
            e.preventDefault();
            uploadBox.style.borderColor = '#e0e0e0';
            uploadBox.style.backgroundColor = '#fafafa';
            
            const files = Array.from(e.dataTransfer.files);
            const imageFiles = files.filter(file => file.type.startsWith('image/'));
            
            imageFiles.forEach(file => {
                if (file.size <= 20 * 1024 * 1024) {
                    uploadedFiles.push(file);
                    displayImage(file);
                } else {
                    alert('파일 크기는 20MB를 초과할 수 없습니다: ' + file.name);
                }
            });
        });
    }

    // 링크 추가/제거 버튼 - 필요없으므로 제거
    // const linkInputGroup = document.querySelector('.link-input-group');
    // const btnAdd = document.querySelector('.btn-add');
    // const btnRemove = document.querySelector('.btn-remove');
    // let linkCount = 1;
    // const maxLinks = 4;

    // 폼 제출 처리
    const postForm = document.getElementById('postForm');
    
    if (postForm) {
        postForm.addEventListener('submit', function(e) {
            e.preventDefault();
            console.log('폼 제출 시작');

            // 필수 필드 요소 확인
            const titleInput = document.querySelector('input[name="title"]');
            const contentTextarea = document.querySelector('textarea[name="content"]');
            const housingTypeSelect = document.querySelector('select[name="housingType"]');

            console.log('titleInput:', titleInput);
            console.log('contentTextarea:', contentTextarea);
            console.log('housingTypeSelect:', housingTypeSelect);

            if (!titleInput || !contentTextarea || !housingTypeSelect) {
                alert('필수 입력 필드를 찾을 수 없습니다. 페이지를 새로고침해주세요.');
                return;
            }

            // 필수 필드 검증
            const title = titleInput.value.trim();
            const content = contentTextarea.value.trim();
            const housingType = housingTypeSelect.value;

            console.log('title:', title);
            console.log('content:', content);
            console.log('housingType:', housingType);

            if (!title) {
                alert('제목을 입력해주세요.');
                titleInput.focus();
                return;
            }

            if (!content) {
                alert('내용을 입력해주세요.');
                contentTextarea.focus();
                return;
            }

            if (!housingType) {
                alert('주거형태를 선택해주세요.');
                housingTypeSelect.focus();
                return;
            }

            // 이미지 업로드 확인
            if (uploadedFiles.length === 0) {
                if (!confirm('이미지 없이 게시글을 작성하시겠습니까?')) {
                    return;
                }
            }

            console.log('검증 완료, FormData 생성 시작');

            // FormData 생성
            const formData = new FormData();

            // 기본 필드 추가 (필수)
            formData.append('title', title);
            formData.append('content', content);
            formData.append('housingType', housingType);

            // 선택 필드 추가 (DTO에 있는 필드만)
            const areaSizeInput = document.querySelector('input[name="areaSize"]');
            if (areaSizeInput && areaSizeInput.value) {
                formData.append('areaSize', areaSizeInput.value);
            }

            const roomCountInput = document.querySelector('input[name="roomCount"]');
            if (roomCountInput && roomCountInput.value) {
                formData.append('roomCount', roomCountInput.value);
            }

            const familyTypeSelect = document.querySelector('select[name="familyType"]');
            if (familyTypeSelect && familyTypeSelect.value) {
                formData.append('familyType', familyTypeSelect.value);
            }

            const familyCountInput = document.querySelector('input[name="familyCount"]');
            if (familyCountInput && familyCountInput.value) {
                formData.append('familyCount', familyCountInput.value);
            }

            const hasPet = document.querySelector('input[name="hasPet"]:checked');
            if (hasPet) {
                formData.append('hasPet', hasPet.value);
            }

            const projectTypeSelect = document.querySelector('select[name="projectType"]');
            if (projectTypeSelect && projectTypeSelect.value) {
                formData.append('projectType', projectTypeSelect.value);
            }

            const isPublic = document.querySelector('input[name="isPublic"]:checked');
            if (isPublic) {
                formData.append('isPublic', isPublic.value);
            }

            // 업로드된 이미지 파일 추가
            uploadedFiles.forEach((file) => {
                formData.append('images', file);
            });

            console.log('FormData 생성 완료, 업로드된 파일 수:', uploadedFiles.length);

            // FormData 내용 확인
            for (let pair of formData.entries()) {
                console.log(pair[0] + ': ' + pair[1]);
            }

            // 제출 버튼 비활성화 (중복 제출 방지)
            const submitBtn = postForm.querySelector('.btn-submit');
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.textContent = '작성 중...';
            }

            console.log('서버로 전송 시작:', postForm.action);

            // 서버로 전송
            fetch(postForm.action, {
                method: 'POST',
                body: formData
            })
            .then(response => {
                console.log('서버 응답:', response);
                if (response.ok) {
                    alert('게시글이 작성되었습니다.');
                    window.location.href = '/posts';
                } else if (response.redirected) {
                    // 리다이렉트된 경우
                    window.location.href = response.url;
                } else {
                    return response.text().then(text => {
                        console.error('서버 에러 응답:', text);
                        throw new Error(text || '게시글 작성에 실패했습니다.');
                    });
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert(error.message || '게시글 작성 중 오류가 발생했습니다.');
                
                // 버튼 다시 활성화
                if (submitBtn) {
                    submitBtn.disabled = false;
                    submitBtn.textContent = '게시글 작성';
                }
            });
        });
    } else {
        console.error('postForm을 찾을 수 없습니다.');
    }
});