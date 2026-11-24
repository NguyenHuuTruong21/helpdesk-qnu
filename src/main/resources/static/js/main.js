/* ============================================================
   HELP DESK QNU - MAIN JAVASCRIPT
   Dùng cho toàn bộ hệ thống (auth, user, admin, public)
   ============================================================ */

(function () {
    'use strict';

    // ====================================================
    // 1. KHỞI TẠO KHI DOM SẴN SÀNG
    // ====================================================
    document.addEventListener('DOMContentLoaded', function () {
        initAutoDismissAlerts();
        initFileInputPreview();
        initActiveNavLink();
        initConfirmForms();
        initTooltips();
        initRememberEmail();
        initInputFilters();
        initSmoothScroll();
        initLiveArticleSearch();
        initAjaxChatForms();
    });

    // ====================================================
    // 2. AUTO-DISMISS ALERTS
    // Tự động đóng alert sau 5 giây
    // ====================================================
    function initAutoDismissAlerts() {
        const alerts = document.querySelectorAll('.alert-dismissible');
        if (!alerts.length) return;

        alerts.forEach(function (alert) {
            setTimeout(function () {
                if (typeof bootstrap !== 'undefined' && bootstrap.Alert) {
                    const bsAlert = bootstrap.Alert.getOrCreateInstance(alert);
                    bsAlert.close();
                }
            }, 5000);
        });
    }

    // ====================================================
    // 3. FILE INPUT PREVIEW
    // Hiển thị tên file đã chọn + validate kích thước
    // ====================================================
    function initFileInputPreview() {
        const fileInputs = document.querySelectorAll('input[type="file"]');
        const MAX_SIZE = 5 * 1024 * 1024; // 5MB

        fileInputs.forEach(function (input) {
            input.addEventListener('change', function (e) {
                const file = e.target.files[0];
                if (!file) return;

                // Validate kích thước
                if (file.size > MAX_SIZE) {
                    alert('File quá lớn! Vui lòng chọn file nhỏ hơn 5MB.');
                    input.value = '';
                    return;
                }

                // Hiển thị tên file
                const next = input.nextElementSibling;
                if (next && next.classList.contains('form-text')) {
                    next.innerHTML =
                        '<i class="bi bi-check-circle-fill text-success"></i> ' +
                        'Đã chọn: <strong>' + escapeHtml(file.name) + '</strong> ' +
                        '<span class="text-muted">(' + formatFileSize(file.size) + ')</span>';
                }

                // Nếu là ảnh, hiển thị preview
                if (file.type.startsWith('image/')) {
                    showImagePreview(input, file);
                }
            });
        });
    }

    function showImagePreview(input, file) {
        // Xóa preview cũ nếu có
        const oldPreview = input.parentElement.querySelector('.image-preview');
        if (oldPreview) oldPreview.remove();

        const reader = new FileReader();
        reader.onload = function (e) {
            const preview = document.createElement('div');
            preview.className = 'image-preview mt-2';
            preview.innerHTML =
                '<img src="' + e.target.result + '" ' +
                'alt="Preview" ' +
                'style="max-width: 200px; max-height: 200px; ' +
                'border-radius: 0.5rem; border: 2px solid #e5e7eb; object-fit: cover;">';
            input.parentElement.appendChild(preview);
        };
        reader.readAsDataURL(file);
    }

    // ====================================================
    // 4. HIGHLIGHT NAV LINK ĐANG ACTIVE
    // ====================================================
    function initActiveNavLink() {
        const currentPath = window.location.pathname;
        const navLinks = document.querySelectorAll('.navbar .nav-link');

        navLinks.forEach(function (link) {
            const href = link.getAttribute('href');
            if (!href || href === '/' || href === '#') return;

            // Nếu URL hiện tại bắt đầu bằng href → active
            if (currentPath === href ||
                (currentPath.startsWith(href) && href.length > 1)) {
                link.classList.add('active');
            }
        });
    }

    // ====================================================
    // 5. CONFIRM FORMS (xóa, submit nguy hiểm)
    // ====================================================
    function initConfirmForms() {
        const forms = document.querySelectorAll('form[data-confirm]');
        forms.forEach(function (form) {
            form.addEventListener('submit', function (e) {
                const message = form.getAttribute('data-confirm') ||
                                'Bạn chắc chắn muốn thực hiện hành động này?';
                if (!confirm(message)) {
                    e.preventDefault();
                }
            });
        });
    }

    // ====================================================
    // 6. BOOTSTRAP TOOLTIPS
    // ====================================================
    function initTooltips() {
        if (typeof bootstrap === 'undefined' || !bootstrap.Tooltip) return;

        const tooltips = document.querySelectorAll('[data-bs-toggle="tooltip"]');
        tooltips.forEach(function (el) {
            new bootstrap.Tooltip(el);
        });
    }

    // ====================================================
    // 7. REMEMBER EMAIL (cho form tra cứu)
    // ====================================================
    function initRememberEmail() {
        const remember = document.getElementById('rememberEmail');
        const emailInput = document.querySelector('input[name="email"]');

        if (!remember || !emailInput) return;

        // Load email đã lưu
        const saved = localStorage.getItem('guestEmail');
        if (saved) {
            emailInput.value = saved;
            remember.checked = true;
        }

        // Lưu khi thay đổi
        remember.addEventListener('change', function () {
            if (this.checked) {
                localStorage.setItem('guestEmail', emailInput.value);
            } else {
                localStorage.removeItem('guestEmail');
            }
        });

        // Cập nhật khi email thay đổi (nếu đã check)
        emailInput.addEventListener('input', function () {
            if (remember.checked) {
                localStorage.setItem('guestEmail', this.value);
            }
        });
    }

    // ====================================================
    // 8. INPUT FILTERS
    // Chỉ cho phép nhập số vào một số trường
    // ====================================================
    function initInputFilters() {
        // Số điện thoại: chỉ cho phép số, +, khoảng trắng
        document.querySelectorAll('input[name="phone"]').forEach(function (input) {
            input.addEventListener('input', function () {
                this.value = this.value.replace(/[^\d+\s]/g, '');
            });
        });

        // Username: chỉ cho phép chữ, số, _
        document.querySelectorAll('input[name="username"]').forEach(function (input) {
            input.addEventListener('input', function () {
                this.value = this.value.replace(/[^a-zA-Z0-9_]/g, '');
            });
        });
    }

    // ====================================================
    // 9. SMOOTH SCROLL
    // ====================================================
    function initSmoothScroll() {
        document.querySelectorAll('a[href^="#"]').forEach(function (anchor) {
            anchor.addEventListener('click', function (e) {
                const target = document.querySelector(this.getAttribute('href'));
                if (target) {
                    e.preventDefault();
                    target.scrollIntoView({ behavior: 'smooth', block: 'start' });
                }
            });
        });
    }

    // ====================================================
    // 10. AJAX ARTICLE SEARCH SUGGESTIONS
    // ====================================================
    function initLiveArticleSearch() {
        const form = document.getElementById('searchForm');
        const input = document.getElementById('searchInput');
        const suggestions = document.getElementById('searchSuggestions');
        const content = document.getElementById('suggestionContent');
        const clearButton = document.getElementById('clearSearchBtn');
        if (!form || !input || !suggestions || !content) return;

        let timer;
        let controller;

        function hideSuggestions() {
            suggestions.classList.add('d-none');
            content.replaceChildren();
        }

        function showMessage(message) {
            const item = document.createElement('div');
            item.className = 'p-3 text-muted small';
            item.textContent = message;
            content.replaceChildren(item);
            suggestions.classList.remove('d-none');
        }

        input.addEventListener('input', function () {
            const keyword = input.value.trim();
            if (clearButton) clearButton.classList.toggle('d-none', keyword.length === 0);

            window.clearTimeout(timer);
            if (controller) controller.abort();

            if (keyword.length < 2) {
                hideSuggestions();
                return;
            }

            timer = window.setTimeout(async function () {
                controller = new AbortController();
                showMessage('Đang tìm bài viết...');

                try {
                    const url = new URL(form.dataset.searchUrl, window.location.origin);
                    url.searchParams.set('keyword', keyword);
                    const response = await fetch(url, {
                        headers: { Accept: 'application/json' },
                        signal: controller.signal
                    });
                    if (!response.ok) throw new Error('Search request failed');

                    const results = await response.json();
                    content.replaceChildren();

                    if (!Array.isArray(results) || results.length === 0) {
                        showMessage('Không tìm thấy bài viết phù hợp.');
                        return;
                    }

                    results.forEach(function (result) {
                        const link = document.createElement('a');
                        link.className = 'd-block p-3 text-decoration-none border-bottom';
                        link.href = form.dataset.articleUrl + encodeURIComponent(result.slug);

                        const title = document.createElement('strong');
                        title.className = 'd-block text-dark';
                        title.textContent = result.title;
                        link.appendChild(title);

                        if (result.summary) {
                            const summary = document.createElement('span');
                            summary.className = 'd-block text-muted small mt-1';
                            summary.textContent = result.summary;
                            link.appendChild(summary);
                        }

                        content.appendChild(link);
                    });
                    suggestions.classList.remove('d-none');
                } catch (error) {
                    if (error.name !== 'AbortError') {
                        showMessage('Không thể tải gợi ý. Nhấn Enter để tìm kiếm.');
                    }
                }
            }, 250);
        });

        if (clearButton) {
            clearButton.addEventListener('click', function () {
                input.value = '';
                input.focus();
                clearButton.classList.add('d-none');
                hideSuggestions();
            });
        }

        document.addEventListener('click', function (event) {
            if (!form.contains(event.target) && !suggestions.contains(event.target)) {
                hideSuggestions();
            }
        });
    }

    // ====================================================
    // 11. AJAX CHAT SUBMISSION
    // ====================================================
    function initAjaxChatForms() {
        document.querySelectorAll('form[data-ajax-chat]').forEach(function (form) {
            form.addEventListener('submit', async function (event) {
                event.preventDefault();

                const button = form.querySelector('[type="submit"]');
                const chatBox = document.getElementById('chatBox');
                if (!chatBox) return;

                let status = form.parentElement.querySelector('[data-chat-status]');
                if (!status) {
                    status = document.createElement('div');
                    status.dataset.chatStatus = '';
                    status.className = 'small mt-2';
                    status.setAttribute('role', 'status');
                    status.setAttribute('aria-live', 'polite');
                    form.insertAdjacentElement('afterend', status);
                }

                if (button) button.disabled = true;
                status.classList.remove('text-danger');
                status.textContent = 'Đang gửi tin nhắn...';

                try {
                    const response = await fetch(form.action, {
                        method: form.method || 'POST',
                        body: new FormData(form),
                        headers: { 'X-Requested-With': 'XMLHttpRequest' }
                    });
                    if (!response.ok) throw new Error('Chat request failed');

                    const page = new DOMParser().parseFromString(await response.text(), 'text/html');
                    const updatedChatBox = page.getElementById('chatBox');
                    if (!updatedChatBox) throw new Error('Chat response is unavailable');

                    chatBox.replaceWith(document.importNode(updatedChatBox, true));

                    const currentStatus = document.querySelector('[data-feedback-status]');
                    const updatedStatus = page.querySelector('[data-feedback-status]');
                    if (currentStatus && updatedStatus) {
                        currentStatus.replaceWith(document.importNode(updatedStatus, true));
                    }

                    document.querySelectorAll('select[name="status"]').forEach(function (select, index) {
                        const updatedSelect = page.querySelectorAll('select[name="status"]')[index];
                        if (updatedSelect) select.value = updatedSelect.value;
                    });

                    form.reset();
                    status.textContent = '';
                    const refreshedChatBox = document.getElementById('chatBox');
                    refreshedChatBox.scrollTop = refreshedChatBox.scrollHeight;
                    form.querySelector('input[name="message"]').focus();
                } catch (error) {
                    status.classList.add('text-danger');
                    status.textContent = 'Không thể gửi tin nhắn. Vui lòng thử lại.';
                } finally {
                    if (button) button.disabled = false;
                }
            });
        });
    }

    // ====================================================
    // 12. HELPERS
    // ====================================================

    /**
     * Escape HTML để chống XSS khi chèn vào innerHTML
     */
    function escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    /**
     * Format kích thước file
     */
    function formatFileSize(bytes) {
        if (bytes < 1024) return bytes + ' B';
        if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
        return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
    }

    // ====================================================
    // 13. EXPORT GLOBAL FUNCTIONS
    // Các hàm gọi từ onclick="..."
    // ====================================================

    /**
     * Toggle hiển thị mật khẩu
     * Dùng: onclick="togglePassword('passwordId', this)"
     */
    window.togglePassword = function (fieldId, button) {
        const input = document.getElementById(fieldId);
        if (!input) return;

        const icon = button.querySelector('i');
        if (!icon) return;

        if (input.type === 'password') {
            input.type = 'text';
            icon.classList.remove('bi-eye');
            icon.classList.add('bi-eye-slash');
        } else {
            input.type = 'password';
            icon.classList.remove('bi-eye-slash');
            icon.classList.add('bi-eye');
        }
    };

    /**
     * Copy mã tracking
     * Dùng: onclick="copyTrackingCode()"
     */
    window.copyTrackingCode = function () {
        const codeEl = document.querySelector('.tracking-code');
        if (!codeEl) return;

        const code = codeEl.textContent.trim();
        const btn = event.target.closest('button');

        // Fallback cho trình duyệt cũ
        if (navigator.clipboard && navigator.clipboard.writeText) {
            navigator.clipboard.writeText(code).then(function () {
                showCopyFeedback(btn);
            }).catch(function () {
                fallbackCopy(code);
                showCopyFeedback(btn);
            });
        } else {
            fallbackCopy(code);
            showCopyFeedback(btn);
        }
    };

    function fallbackCopy(text) {
        const textarea = document.createElement('textarea');
        textarea.value = text;
        textarea.style.position = 'fixed';
        textarea.style.opacity = '0';
        document.body.appendChild(textarea);
        textarea.select();
        try {
            document.execCommand('copy');
        } catch (err) {
            console.error('Không thể copy:', err);
        }
        document.body.removeChild(textarea);
    }

    function showCopyFeedback(btn) {
        if (!btn) return;
        const original = btn.innerHTML;
        btn.innerHTML = '<i class="bi bi-check2"></i> Đã sao chép!';
        btn.classList.remove('btn-outline-primary');
        btn.classList.add('btn-success');

        setTimeout(function () {
            btn.innerHTML = original;
            btn.classList.add('btn-outline-primary');
            btn.classList.remove('btn-success');
        }, 2000);
    }

    /**
     * In trang (dùng cho ticket detail)
     */
    window.printPage = function () {
        window.print();
    };

})();