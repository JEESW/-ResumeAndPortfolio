import React from "react";

const Modal = ({ isOpen, onClose, nickname }) => {
  if (!isOpen) return null; // 모달이 열려있지 않으면 렌더링하지 않음

  const handleOverlayClick = (e) => {
    // 모달 외부(오버레이) 클릭 시 onClose 호출
    if (e.target === e.currentTarget) {
      onClose();
    }
  };

  return (
      <div
          className="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center z-50"
          onClick={handleOverlayClick}
      >
        <div className="bg-white rounded-lg overflow-hidden shadow-lg w-80 relative">
          {/* 모달 상단 */}
          <div className="bg-blue-500 text-white text-center py-4">
            <div className="text-4xl">
              <i className="fas fa-user-circle"></i>
            </div>
            <p className="mt-2 text-lg">{nickname}</p>
          </div>

          {/* 모달 내용 */}
          <div className="p-6 text-center">
            <button
                className="block w-full py-2 mb-4 border border-gray-300 rounded-lg hover:bg-gray-100"
                onClick={() => alert("회원 정보 수정 페이지로 이동")}
            >
              회원 정보 수정
            </button>
            <button
                className="block w-full py-2 border border-red-300 text-red-600 rounded-lg hover:bg-red-100"
                onClick={() => alert("회원 탈퇴 진행")}
            >
              회원 탈퇴
            </button>
          </div>

          {/* 닫기 버튼 */}
          <button
              className="absolute top-4 right-4 text-white hover:text-gray-900"
              onClick={onClose}
          >
            ×
          </button>
        </div>
      </div>
  );
};

export default Modal;