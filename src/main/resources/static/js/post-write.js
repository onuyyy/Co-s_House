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

    // 링크 추가/제거 버튼
    const linkInputGroup = document.querySelector('.link-input-group');
    const btnAdd = document.querySelector('.btn-add');
    const btnRemove = document.querySelector('.btn-remove');
    let linkCount = 1;
    const maxLinks = 4;

    if (btnAdd) {
        btnAdd.addEventListener('click', function() {
            if (linkCount >= maxLinks) {
                alert('링크는 최대 4개까지 추가할 수 있습니다.');
                return;
            }

            const newLinkGroup = linkInputGroup.cloneNode(true);
            const inputs = newLinkGroup.querySelectorAll('input');
            inputs.forEach(input => input.value = '');
            
            linkInputGroup.parentNode.insertBefore(newLinkGroup, linkInputGroup.nextSibling);
            linkCount++;
        });
    }

    if (btnRemove) {
        btnRemove.addEventListener('click', function() {
            if (linkCount <= 1) {
                const inputs = linkInputGroup.querySelectorAll('input');
                inputs.forEach(input => input.value = '');
                return;
            }

            linkInputGroup.parentNode.removeChild(linkInputGroup);
            linkCount--;
        });
    }

    // 폼 제출 처리
    const postForm = document.getElementById('postForm');
    
    if (postForm) {
        postForm.addEventListener('submit', function(e) {
            e.preventDefault();

            // 필수 필드 검증
            const title = document.querySelector('input[name="title"]').value.trim();
            const content = document.querySelector('textarea[name="content"]').value.trim();
            const housingType = document.querySelector('select[name="housingType"]').value;

            if (!title) {
                alert('제목을 입력해주세요.');
                return;
            }

            if (!content) {
                alert('내용을 입력해주세요.');
                return;
            }

            if (!housingType) {
                alert('주거형태를 선택해주세요.');
                return;
            }

            // FormData 생성
            const formData = new FormData(postForm);

            // 업로드된 이미지 추가
            uploadedFiles.forEach((file, index) => {
                formData.append('images', file);
            });

            // 서버로 전송
            fetch(postForm.action, {
                method: 'POST',
                body: formData
            })
            .then(response => {
                if (response.ok) {
                    alert('게시글이 저장되었습니다.');
                    window.location.href = '/posts';
                } else {
                    return response.json().then(data => {
                        throw new Error(data.message || '게시글 저장에 실패했습니다.');
                    });
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert(error.message);
            });
        });
    }
});