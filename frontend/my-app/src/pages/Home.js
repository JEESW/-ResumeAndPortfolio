import React from "react";

const Home = () => {
  return (
      <div className="min-h-screen bg-gray-100 flex items-center justify-center">
        <div className="bg-white shadow-lg rounded-lg p-8 max-w-2xl text-center">
          {/* 이름 및 직업 */}
          <h1 className="text-4xl font-bold text-blue-600 mb-4">안녕하세요! 지승우입니다.</h1>
          <h2 className="text-xl text-gray-700 mb-6">열정적인 백엔드 개발자입니다.</h2>

          {/* 자기소개 */}
          <p className="text-gray-600 leading-relaxed">
            제 이력서와 포토폴리오에 관심을 가져주셔서 감사합니다! 저는 확장 가능하고 효율적인 시스템을 구축하는 데 큰 관심을 갖고 있는 백엔드 개발자입니다. 새로운 기술을 배우고 지속적으로 기술을 향상시키는 것을 좋아합니다. 제 이력서와 포트폴리오를 통해 저의 역량을 확인하고 평가해 주세요!
          </p>

          {/* 링크 버튼 */}
          <div className="mt-8">
            <a
                href="/resume"
                className="bg-blue-600 text-white px-6 py-2 rounded-lg shadow-md hover:bg-blue-700 transition"
            >
              View My Resume
            </a>
            <a
                href="/portfolio"
                className="ml-4 bg-gray-800 text-white px-6 py-2 rounded-lg shadow-md hover:bg-gray-700 transition"
            >
              View My Portfolio
            </a>
          </div>
        </div>
      </div>
  );
};

export default Home;