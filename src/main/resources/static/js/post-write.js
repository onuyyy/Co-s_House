// 포스트 작성 페이지 JavaScript

document.addEventListener('DOMContentLoaded', function() {
    // 가이드 박스 토글
    const guideToggle = document.querySelector('.guide-toggle');
    const guideContent = document.querySelector('.guide-content');
    
    if (guideToggle && guideContent) {
        guideToggle.addEventListener('click', function() {
            if (guideContent.classList.contains('hidden')) {
                guideContent.classList.remove('hidden');
                guideToggle.textContent = '▲';
            } else {
                guideContent.classList.add('hidden');
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
                if (file.size > 20 * 1024 * 1024) {
                    alert('파일 크기는 20MB를 초과할 수 없습니다: ' + file.name);
                    return;
                }

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

    // 상품 관련 변수
    let selectedProducts = [];
    let allProducts = [];

    // 상품 모달 열기
    window.openProductModal = function() {
        const modal = document.getElementById('productModal');
        modal.classList.add('show');
        loadProducts();
    };

    // 상품 모달 닫기
    window.closeProductModal = function() {
        const modal = document.getElementById('productModal');
        modal.classList.remove('show');
    };

    // 상품 목록 불러오기 (AJAX)
    function loadProducts() {
        const productList = document.getElementById('productList');
        productList.innerHTML = '<p class="loading">상품을 불러오는 중...</p>';

        fetch('/posts/products', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('상품을 불러오는데 실패했습니다.');
            }
            return response.json();
        })
        .then(products => {
            allProducts = products;
            displayProducts(products);
        })
        .catch(error => {
            console.error('Error:', error);
            productList.innerHTML = '<p class="loading">주문한 상품이 없습니다.</p>';
        });
    }

    // 상품 목록 표시
    function displayProducts(products) {
        const productList = document.getElementById('productList');
        
        if (products.length === 0) {
            productList.innerHTML = '<p class="loading">주문한 상품이 없습니다.</p>';
            return;
        }

        productList.innerHTML = products.map(product => `
            <div class="product-item ${selectedProducts.includes(product.productId) ? 'selected' : ''}" 
                 data-product-id="${product.productId}"
                 onclick="toggleProductSelection(${product.productId})">
                <div class="product-item-image">
                    <img src="${product.mainImageUrl || '/images/default-product.jpg'}" 
                         alt="${product.productTitle}"
                         onerror="this.src='/images/default-product.jpg'">
                </div>
                <div class="product-item-info">
                    <p class="product-item-title">${product.productTitle}</p>
                    <p class="product-item-price">${formatPrice(product.originalPrice)}원</p>
                </div>
            </div>
        `).join('');
    }

    // 가격 포맷팅
    function formatPrice(price) {
        return price.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',');
    }

    // 상품 선택 토글
    window.toggleProductSelection = function(productId) {
        const index = selectedProducts.indexOf(productId);
        
        if (index > -1) {
            selectedProducts.splice(index, 1);
        } else {
            selectedProducts.push(productId);
        }

        // UI 업데이트
        const productItem = document.querySelector(`.product-item[data-product-id="${productId}"]`);
        productItem.classList.toggle('selected');
    };

    // 상품 선택 완료
    window.confirmProductSelection = function() {
        displaySelectedProducts();
        closeProductModal();
    };

    // 선택된 상품 표시
    function displaySelectedProducts() {
        const container = document.getElementById('selectedProducts');
        const selectedProductsData = allProducts.filter(p => selectedProducts.includes(p.productId));

        container.innerHTML = selectedProductsData.map(product => `
            <div class="selected-product-item" onclick="goToProduct(${product.productId})">
                <div class="selected-product-image">
                    <img src="${product.mainImageUrl || '/images/default-product.jpg'}" 
                         alt="${product.productTitle}"
                         onerror="this.src='/images/default-product.jpg'">
                    <button class="selected-product-remove" 
                            onclick="event.stopPropagation(); removeSelectedProduct(${product.productId})"
                            type="button">×</button>
                </div>
                <div class="selected-product-title">${product.productTitle}</div>
            </div>
        `).join('');
    }

    // 선택된 상품 제거
    window.removeSelectedProduct = function(productId) {
        const index = selectedProducts.indexOf(productId);
        if (index > -1) {
            selectedProducts.splice(index, 1);
            displaySelectedProducts();
        }
    };

    // 상품 상세 페이지로 이동
    window.goToProduct = function(productId) {
        window.open(`/product/${productId}`, '_blank');
    };

    // 폼 제출 처리
    const postForm = document.getElementById('postForm');
    
    if (postForm) {
        postForm.addEventListener('submit', function(e) {
            e.preventDefault();

            const titleInput = document.querySelector('input[name="title"]');
            const contentTextarea = document.querySelector('textarea[name="content"]');
            const housingTypeSelect = document.querySelector('select[name="housingType"]');

            if (!titleInput || !contentTextarea || !housingTypeSelect) {
                alert('필수 입력 필드를 찾을 수 없습니다.');
                return;
            }

            const title = titleInput.value.trim();
            const content = contentTextarea.value.trim();
            const housingType = housingTypeSelect.value;

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

            if (uploadedFiles.length === 0) {
                if (!confirm('이미지 없이 게시글을 작성하시겠습니까?')) {
                    return;
                }
            }

            const formData = new FormData();

            formData.append('title', title);
            formData.append('content', content);
            formData.append('housingType', housingType);

            const areaSizeInput = document.querySelector('input[name="areaSize"]');
            if (areaSizeInput && areaSizeInput.value) formData.append('areaSize', areaSizeInput.value);

            const roomCountInput = document.querySelector('input[name="roomCount"]');
            if (roomCountInput && roomCountInput.value) formData.append('roomCount', roomCountInput.value);

            const familyTypeSelect = document.querySelector('select[name="familyType"]');
            if (familyTypeSelect && familyTypeSelect.value) formData.append('familyType', familyTypeSelect.value);

            const familyCountInput = document.querySelector('input[name="familyCount"]');
            if (familyCountInput && familyCountInput.value) formData.append('familyCount', familyCountInput.value);

            const hasPet = document.querySelector('input[name="hasPet"]:checked');
            if (hasPet) formData.append('hasPet', hasPet.value);

            const projectTypeSelect = document.querySelector('select[name="projectType"]');
            if (projectTypeSelect && projectTypeSelect.value) formData.append('projectType', projectTypeSelect.value);

            const isPublic = document.querySelector('input[name="isPublic"]:checked');
            if (isPublic) formData.append('isPublic', isPublic.value);

            // 이미지 파일 추가
            uploadedFiles.forEach((file) => {
                formData.append('images', file);
            });

            // 선택된 상품 ID 추가
            selectedProducts.forEach((productId) => {
                formData.append('productIds', productId);
            });

            console.log('선택된 상품 수:', selectedProducts.length);

            const submitBtn = postForm.querySelector('.btn-submit');
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.textContent = '작성 중...';
            }

            fetch(postForm.action, {
                method: 'POST',
                body: formData
            })
            .then(response => {
                if (response.ok || response.redirected) {
                    alert('게시글이 작성되었습니다.');
                    window.location.href = '/posts';
                } else {
                    return response.text().then(text => {
                        throw new Error(text || '게시글 작성에 실패했습니다.');
                    });
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert(error.message || '게시글 작성 중 오류가 발생했습니다.');
                
                if (submitBtn) {
                    submitBtn.disabled = false;
                    submitBtn.textContent = '게시글 작성';
                }
            });
        });
    }
});