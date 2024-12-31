import React from "react";

const Footer = () => {
  return (
      <footer className="bg-gray-800 text-white py-6">
        <div className="container mx-auto px-4 flex flex-col md:flex-row justify-between items-center">
          {/* 왼쪽 섹션: 로고 */}
          <div className="text-lg font-bold">
            Resume&Portfolio
          </div>

          {/* 가운데 섹션: 네비게이션 */}
          <nav className="my-4 md:my-0">
            <ul className="flex space-x-6">
              <li>
                <a href="/" className="text-gray-300 hover:text-white">
                  Home
                </a>
              </li>
              <li>
                <a href="/resume" className="text-gray-300 hover:text-white">
                  Resume
                </a>
              </li>
              <li>
                <a href="/portfolio" className="text-gray-300 hover:text-white">
                  Portfolio
                </a>
              </li>
              <li>
                <a href="/faq" className="text-gray-300 hover:text-white">
                  FAQ
                </a>
              </li>
            </ul>
          </nav>

          {/* 오른쪽 섹션: 만든이 */}
          <div className="text-sm text-gray-400">
            Made by Seungwoo-Ji
          </div>
        </div>
      </footer>
  );
};

export default Footer;