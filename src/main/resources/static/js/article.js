// 글 삭제 버튼
const deleteBtn = document.getElementById('delete-btn');
if (deleteBtn) {
    deleteBtn.addEventListener('click', event => {
        let id = document.getElementById('article-id').value;
        function success() {
            alert('삭제가 완료되었습니다.');
            location.replace('/articles');
        }

        function fail() {
            alert('삭제 실패했습니다.');
            location.replace('/articles');
        }

        httpRequest('DELETE', `/api/articles/${id}`, null, success, fail);
    });
}

// 글 수정 버튼
const modifyBtn = document.getElementById("modify-btn");
if (modifyBtn) {
    modifyBtn.addEventListener('click', () => {
        let params = new URLSearchParams(location.search);
        let id = params.get('id');

        body = JSON.stringify({
            title: document.getElementById('title').value,
            content: document.getElementById('content').value
        })

        function success() {
            alert('수정 완료되었습니다.');
            location.replace(`/articles/${id}`);
        }

        function fail() {
            alert('수정 실패했습니다.');
            location.replace(`/articles/${id}`);
        }

        httpRequest('PUT', `/api/articles/${id}`, body, success, fail);
    });
}

// 글 작성 버튼
const createBtn = document.getElementById("create-btn");
if (createBtn) {
    createBtn.addEventListener("click", () => {
        body = JSON.stringify({
            title: document.getElementById("title").value,
            content: document.getElementById("content").value
        });
        function success() {
            alert("등록이 완료되었습니다.");
            location.replace("/articles");
        }
        function fail() {
            alert("등록이 실패헀습니다.");
            location.replace("/article");
        }

        httpRequest("POST", "/api/articles", body, success, fail);
    });
}

// Cookie 취득 function
function getCookie(key) {
    let result;
    let cookie = document.cookie.split(";");
    cookie.some(function (item) {
        let dic = item.trim().split("=");

        if (key === dic[0]) {
            result = dic[1];
            return true;
        }
    });
    
    return result;
}

// HTTP 요청 function
function httpRequest(method, url, body, success, fail) {
    fetch(url, {
        method: method,
        headers: {
            Authorization: "Bearer " + localStorage.getItem("access_token"),
            "Content-Type": "application/json"
        },
        body: body
    }).then((response) => {
        if (response.status === 200 || response.status === 201) {
            return success();
        }
        const refresh_token= getCookie("refresh_token");
        if (response.status === 401 && refresh_token) {
            fetch("/api/token", {
                method: "POST",
                headers: {
                    Authorization: "Bearer" + localStorage.getItem("access_token"),
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    refreshToken: getCookie("refresh_token")
                })
            })
            .then((res) => {
                if (res.ok) {
                    return res.json();
                }
            })
            .then((result) => {
                // 재발급이 성공하면 localStorage 값을 새로운 accessToken으로 교체
                localStorage.setItem("access_token", result.accessToken);
                httpRequest(method, url, body, success, fail);
            })
            .catch(() => fail());
        } else {
            return fail();
        }
    });
}