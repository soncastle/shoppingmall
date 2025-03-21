// apiClient.js
const API_BASE_URL = 'http://localhost:8080';

// 기본 헤더와 옵션 설정
const defaultOptions = {
  headers: {
    'Content-Type': 'application/json'
  }
};

// 기본 fetch 래퍼 함수
async function apiRequest(endpoint, options = {}) {
  const url = `${API_BASE_URL}${endpoint}`;

  // 기본 옵션과 사용자 옵션 병합
  const requestOptions = {
    ...defaultOptions,
    ...options,
    headers: {
      ...defaultOptions.headers,
      ...options.headers
    }
  };

  try {
    const response = await fetch(url, requestOptions);

    // 응답 상태 확인
    if (!response.ok) {
      return await response.json();
    }

    // JSON 응답 파싱
    return await response.json();
  } catch (error) {
    console.error('API 요청 실패:', error);
    throw error;
  }
}

// HTTP 메서드별 편의 함수들
export const apiClient = {
  get: (endpoint, options = {}) =>
      apiRequest(endpoint, { ...options, method: 'GET' }),

  post: (endpoint, data, options = {}) =>
      apiRequest(endpoint, {
        ...options,
        method: 'POST',
        body: JSON.stringify(data)
      }),

  put: (endpoint, data, options = {}) =>
      apiRequest(endpoint, {
        ...options,
        method: 'PUT',
        body: JSON.stringify(data)
      }),

  delete: (endpoint, options = {}) =>
      apiRequest(endpoint, { ...options, method: 'DELETE' })
};