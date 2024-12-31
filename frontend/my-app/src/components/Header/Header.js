import React from "react";

const Header = () => {
  return (
      <header className="bg-white shadow-md">
        <div className="container mx-auto px-4 py-3 flex justify-between items-center">
          {/* 로고 */}
          <div className="text-lg font-bold text-blue-600">
            Resume&Portfolio
          </div>

          {/* 네비게이션 */}
          <nav className="hidden md:flex space-x-6">
            <a href="/" className="text-gray-700 hover:text-blue-600">
              Home
            </a>
            <a href="/resume" className="text-gray-700 hover:text-blue-600">
              Resume
            </a>
            <a href="/portfolio" className="text-gray-700 hover:text-blue-600">
              Portfolio
            </a>
            <a href="/faq" className="text-gray-700 hover:text-blue-600">
              FAQ
            </a>
          </nav>

          {/* 로그인 및 버튼 */}
          <div className="flex items-center space-x-4">
            <button className="text-gray-700 hover:text-blue-600">Log In</button>
            <button className="bg-gray-800 text-white px-4 py-2 rounded-lg hover:bg-gray-700">
              My Page
            </button>
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
  );
};

export default Header;