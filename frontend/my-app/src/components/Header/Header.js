import React, {useState, useEffect} from "react";
import {useNavigate} from "react-router-dom";
import axios from "axios";
import Modal from "../Modal/Modal";

const Header = () => {
  const navigate = useNavigate();
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [nickname, setNickname] = useState("");

  // 로그인 상태 확인 (로컬 스토리지에서 JWT 확인)
  useEffect(() => {
    const token = localStorage.getItem("accessToken");
    setIsLoggedIn(!!token); // 토큰이 존재하면 로그인 상태로 설정

    // 사용자 정보 조회 API 호출
    if (token) {
      fetchUserInfo();
    }
  }, []);

  // 현재 사용자 정보 조회
  const fetchUserInfo = async () => {
    try {
      const response = await axios.get(
          "https://www.jsw-resumeandportfolio.com/api/users/me",
          {
            headers: {
              Authorization: `Bearer ${localStorage.getItem("accessToken")}`, // JWT 토큰을 헤더에 추가
            },
          }
      );
      setNickname(response.data.nickname); // 닉네임 설정
    } catch (error) {
      console.error("Failed to fetch user info:", error);
      if (error.response && error.response.status === 401) {
        alert("로그인이 필요합니다.");
        navigate("/login");
      }
    }
  };

  // 로그아웃 핸들러
  const handleLogout = async () => {
    try {
      // 백엔드로 로그아웃 요청
      await axios.post(
          "https://www.jsw-resumeandportfolio.com/api/users/logout",
          {}, // 로그아웃 요청에 데이터 필요 없음
          {withCredentials: true} // 쿠키 포함 설정
      );

      // 로컬 스토리지에서 토큰 삭제
      localStorage.removeItem("accessToken");

      // 로그인 상태 초기화
      setIsLoggedIn(false);

      // 홈으로 이동
      navigate("/");
    } catch (err) {
      console.error("Logout failed:", err);
      alert("로그아웃 중 문제가 발생했습니다.");
    }
  };

  return (
      <>
        <header className="bg-white shadow-md">
          <div
              className="container mx-auto px-4 py-3 flex justify-between items-center">
            {/* 로고 */}
            <div className="text-lg font-bold text-blue-600">
              <a href="/" className="text-blue-600">
                Resume&Portfolio
              </a>
            </div>

            {/* 네비게이션 */}
            <nav className="hidden md:flex space-x-6">
              <a href="/" className="text-gray-700 hover:text-blue-600">
                Home
              </a>
              <a href="/resume" className="text-gray-700 hover:text-blue-600">
                Resume
              </a>
              <a href="/portfolio"
                 className="text-gray-700 hover:text-blue-600">
                Portfolio
              </a>
              <a href="/faq" className="text-gray-700 hover:text-blue-600">
                FAQ
              </a>
            </nav>

            {/* 로그인/로그아웃 및 버튼 */}
            <div className="flex items-center space-x-4">
              {!isLoggedIn ? (
                  <>
                    {/* 로그인 버튼 */}
                    <button
                        onClick={() => navigate("/login")}
                        className="text-gray-700 hover:text-blue-600"
                    >
                      Login
                    </button>
                    {/* 회원가입 버튼 */}
                    <button
                        onClick={() => navigate("/signup")}
                        className="bg-gray-800 text-white px-4 py-2 rounded-lg hover:bg-gray-700"
                    >
                      Sign Up
                    </button>
                  </>
              ) : (
                  <>
                    {/* 로그아웃 버튼 */}
                    <button
                        onClick={handleLogout}
                        className="text-gray-700 hover:text-blue-600"
                    >
                      Logout
                    </button>
                    {/* 마이페이지 버튼 */}
                    <button
                        onClick={() => setIsModalOpen(true)}
                        className="bg-gray-800 text-white px-4 py-2 rounded-lg hover:bg-gray-700"
                    >
                      My Page
                    </button>
                  </>
              )}
            </div>

            {/* 모바일 메뉴 */}
            <div className="md:hidden">
              <button className="text-gray-700">
                <svg
                    xmlns="http://www.w3.org/2000/svg"
                    className="h-6 w-6"
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                    strokeWidth={2}
                >
                  <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      d="M4 6h16M4 12h16m-7 6h7"
                  />
                </svg>
              </button>
            </div>
          </div>
        </header>

        {/* 모달 */}
        <Modal
            isOpen={isModalOpen}
            onClose={() => setIsModalOpen(false)}
            nickname={nickname}
        />
      </>
  );
};

export default Header;