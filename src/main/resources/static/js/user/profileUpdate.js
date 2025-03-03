

async function loadUserData() {

  try {
    const response = await fetch("/api/users/me/profile",
        {credentials: "include"})
    if (!response.ok) {
      throw new Error("사용자 정보를 불러오지 못했습니다.")
    }

    const data = await response.json();
    console.log(data)
    const userIdInput = document.querySelector(".profile-user")
    const nickNameInput = document.querySelector("#nickname")
    const emailInput = document.querySelector("#email")
    const addressInput = document.querySelector("#address")

    userIdInput.innerText = data.userId
    nickNameInput.value = data.nickname
    emailInput.value = data.email
    addressInput.value = data.address

  } catch (e) {
    console.log(e)
  }
}

loadUserData()
.then(() => {
  console.log("사용자 정보 완료")
}).catch((error) => {
  console.error("사용자 정보 로드 실패", error)
})

const profileUpdateForm = document.querySelector("#profileUpdateForm")
const submitButton = document.querySelector(".save-btn")

profileUpdateForm.addEventListener('input', (e) => {
  submitButton.disabled = false;
})

profileUpdateForm.addEventListener("submit", async (e) => {
  e.preventDefault();

  const redirect = confirm("변경사항을 적용 하시겠습니까?")
  if (redirect) {
    const formData = new FormData(e.target);
    const formObj = Object.fromEntries(formData);
    console.log(formObj)

    const response = await fetch("/api/users/me/profile", {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(formObj)
    })
    if (!response.ok) {
      const nickNameError = document.querySelector("#nickname-error")
      const emailError = document.querySelector("#email-error")
      const addressError = document.querySelector("#address-error")
      const result = await response.json();

      nickNameError.innerHTML = "";
      emailError.innerHTML = "";
      addressError.innerHTML = "";

      if (result.message) {
          if (result.message.nickname) {
            nickNameError.textContent = result.message.nickname;
          }
          if (result.message.email) {
            emailError.textContent = result.message.email;
          }
          if (result.message.address) {
            addressError.textContent = result.message.address;
          }
      }

    } else {
      const result = await response.json();
      console.log(result)
      alert("수정되었습니다");
      window.location.href = "/users/me";

    }
  }

})

// 회원 탈퇴

const removeBtn = document.querySelector(".removeBtn")

removeBtn.addEventListener("click", async (e) => {

  const password = prompt("비밀번호를 입력해주세요.")

  if (password) {
    const userOk = confirm("정말 탈퇴 하시겠습니까? 탈퇴시 복구가 되지 않습니다.")

    if (userOk) {
      try {
        const response = await fetch("/api/users", {
          method: "DELETE",
          headers: {
            "Content-Type": "application/json"
          },
          body: JSON.stringify({password: password})
        })

        if (response.status === 400) {
          alert("소셜회원은 탈퇴 불가합니다.")
        }

        if (response.status === 401) {
          alert("비밀번호가 일치하지 않습니다.")
        }

        if (response.status === 200) {
          alert("회원 탈퇴가 완료되었습니다.")
          window.location.href = "/home";
        }
      } catch (e) {
        console.log(e)
      }
    }

  }

})