/* profile.js  유저 프로필에 기능 들 및 필요한 정보 불러오고 , 닉네임 수정 등 기능 */
import {modalUtil} from "./modal.js";
import {loadTopInfo} from "../common/api-declare.js";
import {loadWishlist} from "../common/api-declare.js";
import {loadPetList} from "../common/api-declare.js";

document.addEventListener("DOMContentLoaded" ,  async () =>{

  await loadTopInfo();
  await loadWishlist();
  await loadPetList();

})

document.addEventListener("DOMContentLoaded", async (e) => {
  modalUtil();
});

