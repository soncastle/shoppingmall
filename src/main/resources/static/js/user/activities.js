import {loadTopInfo} from "../common/api-declare.js";
import {modalUtil} from "./modal.js";
import {mypageTopInfo} from "../common/api-declare.js";
/* activities.js 유저의 활동 기록을 불러오는 js */


document.addEventListener("DOMContentLoaded", async () => {

    modalUtil()
    await loadTopInfo()


    let cacheData = {}; // 캐싱할 데이터 저장
    const tabs = document.querySelectorAll(".consult__tab .tab li > a");
    const contentContainer = document.querySelector(".activities_content ul");
    const emptyMessages = {
        boards: "작성한 글이 없습니다.",
        comments: "작성한 댓글이 없습니다.",
        likes: "좋아요한 글이 없습니다."
    };

    await fetchData("boards"); // 초기 로드

    tabs.forEach(tab => {
        tab.addEventListener("click", async function (event) {
            event.preventDefault();

            tabs.forEach(t => t.classList.remove("active"));
            this.classList.add("active");

            const tabType = this.dataset.type;
            if (!cacheData[tabType]) {
                await fetchData(tabType);
            } else {
                renderContent(tabType); // 캐싱된 데이터가 있으면 바로 렌더링
            }
        });
    });

    async function fetchData(type) {
        try {
            const response = await fetch(`/api/users/me/activities/${type}`);
            const data = await response.json();
            cacheData[type] = data.data; // ✅ 여기서 data.data만 저장해야 올바르게 접근 가능

            console.log("Fetched data:", data);
            console.log("Cached data:", cacheData);

            renderContent(type);
        } catch (error) {
            console.error("데이터 불러오기 실패:", error);
            contentContainer.innerHTML = `<p>데이터를 불러오는 중 오류가 발생했습니다.</p>`;
        }
    }

    function renderContent(type) {
        contentContainer.innerHTML = "";

        const items = cacheData[type]?.[type]; // ✅ data.data를 저장했으므로 cacheData[type][type]이 되어야 함
        if (!items || items.length === 0) {
            contentContainer.innerHTML = `
                <div class="myboard_empty">
                    <p>${emptyMessages[type]}</p>
                    <a class="btn_line" href="/board/board">커뮤니티 둘러보기</a>
                </div>
            `;
        } else {
            contentContainer.innerHTML = items.map(item => `
                <li>
                    <a href="/board/read?boardId=${item.boardId}">
                        <div class="wrap">
                            <div class="top">
                                <span>${item.categoryId}</span>
                                <h3 class="tit">${item.title}</h3>
                            </div>
                            <div class="bottom">
                                <div class="date">
                                    <span>${item.nickname}</span>
                                    <span>${item.createAt}</span>
                                    <span>조회 수 <strong>${item.viewCount}</strong></span>
                                </div>
                                <div class="like">
                                    <span>💗 <strong>${item.likeCount}</strong></span>
                                    <span>💬 <strong>${item.commentCount}</strong></span>
                                </div>
                            </div>
                        </div>
                    </a>
                </li>
            `).join("");
        }
    }
});
