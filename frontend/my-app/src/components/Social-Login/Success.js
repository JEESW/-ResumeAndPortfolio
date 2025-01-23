import React, {useEffect} from "react";
import axios from "axios";
import {useNavigate} from "react-router-dom";

const Success = () => {
  const navigate = useNavigate();

  useEffect(() => {
    const reissueTokens = async () => {
      try {
        const response = await axios.post(
            "https://www.jsw-resumeandportfolio.com/api/users/reissue",
            {},
            {withCredentials: true} // 쿠키를 포함하여 요청
        );

        // Access Token을 로컬 스토리지에 저장
        const accessToken = response.headers["authorization"];
        if (accessToken) {
          localStorage.setItem("accessToken", accessToken);
          navigate("/");
        } else {
          alert("Access Token 재발급에 실패했습니다. 다시 로그인하세요.");
          navigate("/login");
        }
      } catch (error) {
        console.error("Failed to reissue tokens:", error);
        alert("로그인 과정에서 문제가 발생했습니다.");
        navigate("/login");
      }
    };

    // 토큰 재발급 요청
    reissueTokens();
  }, [navigate]);

  return (
      <div>
        <h1>로그인 처리 중...</h1>
      </div>
  );
};

export default Success;