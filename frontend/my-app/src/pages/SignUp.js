import React, { useState } from "react";
import axios from "axios";

const SignUp = () => {
  const [step, setStep] = useState(1); // 단계 관리: 1-이메일 입력, 2-토큰 확인, 3-회원가입 완료
  const [email, setEmail] = useState("");
  const [emailMessage, setEmailMessage] = useState("");
  const [emailValid, setEmailValid] = useState(null);

  const [token, setToken] = useState("");
  const [tokenMessage, setTokenMessage] = useState("");
  const [tokenValid, setTokenValid] = useState(null);

  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [passwordMatch, setPasswordMatch] = useState(null);

  const [nickname, setNickname] = useState("");

  // 이메일 유효성 검사 함수
  const isEmailValid = (email) => {
    const emailRegex =
        /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    return emailRegex.test(email);
  };

  // 이메일 중복 확인 및 인증 요청
  const handleEmailCheck = async () => {
    if (!isEmailValid(email)) {
      setEmailValid(false);
      setEmailMessage("올바른 이메일 형식을 입력하세요.");
      return;
    }

    try {
      // 백엔드의 UserRegisterRequest 요구사항을 맞추기 위해 임의 값 포함
      await axios.post("https://www.jsw-resumeandportfolio.com/api/users/register/initiate", {
        email,
        password: "temporary123", // 임시 비밀번호
        confirmPassword: "temporary123", // 임시 비밀번호 확인
        nickname: "temporaryUser" // 임시 닉네임
      });
      setEmailValid(true);
      setEmailMessage("인증 이메일이 발송되었습니다. 이메일을 확인하세요!");
      setStep(2);
    } catch (err) {
      setEmailValid(false);
      setEmailMessage(err.response?.data?.message || "오류가 발생했습니다.");
    }
  };

  // 토큰 확인
  const handleTokenCheck = async () => {
    try {
      await axios.post(
          "https://www.jsw-resumeandportfolio.com/api/users/register/complete",
          { token }
      );
      setTokenValid(true);
      setTokenMessage("이메일 인증이 완료되었습니다.");
      setStep(3);
    } catch (err) {
      setTokenValid(false);
      setTokenMessage(err.response?.data?.message || "인증 토큰이 유효하지 않습니다.");
    }
  };

  // 비밀번호 일치 여부 확인
  const handlePasswordChange = (value) => {
    setPassword(value);
    setPasswordMatch(value === confirmPassword);
  };

  const handleConfirmPasswordChange = (value) => {
    setConfirmPassword(value);
    setPasswordMatch(password === value);
  };

  // 회원가입 요청
  const handleSignUp = async () => {
    if (!passwordMatch || !nickname) {
      alert("모든 입력이 올바른지 확인해주세요.");
      return;
    }

    try {
      await axios.post("https://www.jsw-resumeandportfolio.com/api/users/register/complete", {
        token,
        password,
        nickname,
      });
      alert("회원가입이 완료되었습니다!");
    } catch (err) {
      alert(err.response?.data?.message || "회원가입 중 오류가 발생했습니다.");
    }
  };

  return (
      <div className="min-h-screen bg-gray-100 flex items-center justify-center">
        <div className="bg-white p-8 rounded-lg shadow-lg w-full max-w-md">
          {step === 1 && (
              <>
                <h2 className="text-2xl font-bold mb-4 text-gray-800">회원 가입</h2>
                <div>
                  <label htmlFor="email" className="block text-gray-700">Email</label>
                  <input
                      type="email"
                      id="email"
                      className="w-full px-4 py-2 border rounded-lg"
                      placeholder="이메일을 입력해 주세요."
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                  />
                  <button
                      type="button"
                      onClick={handleEmailCheck}
                      className="mt-2 bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700"
                  >
                    인증 요청
                  </button>
                  {emailMessage && (
                      <p className={`text-sm ${emailValid ? "text-green-600" : "text-red-600"}`}>
                        {emailMessage}
                      </p>
                  )}
                </div>
              </>
          )}

          {step === 2 && (
              <>
                <h2 className="text-2xl font-bold mb-4 text-gray-800">이메일 인증</h2>
                <div>
                  <label htmlFor="token" className="block text-gray-700">인증 토큰</label>
                  <input
                      type="text"
                      id="token"
                      className="w-full px-4 py-2 border rounded-lg"
                      placeholder="이메일로 받은 인증 토큰을 입력하세요."
                      value={token}
                      onChange={(e) => setToken(e.target.value)}
                  />
                  <button
                      type="button"
                      onClick={handleTokenCheck}
                      className="mt-2 bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700"
                  >
                    인증 확인
                  </button>
                  {tokenMessage && (
                      <p className={`text-sm ${tokenValid ? "text-green-600" : "text-red-600"}`}>
                        {tokenMessage}
                      </p>
                  )}
                </div>
              </>
          )}

          {step === 3 && (
              <>
                <h2 className="text-2xl font-bold mb-4 text-gray-800">회원 가입 완료</h2>
                <div>
                  <label htmlFor="password" className="block text-gray-700">Password</label>
                  <input
                      type="password"
                      id="password"
                      className="w-full px-4 py-2 border rounded-lg"
                      placeholder="비밀번호를 입력하세요."
                      value={password}
                      onChange={(e) => handlePasswordChange(e.target.value)}
                  />
                </div>
                <div>
                  <label htmlFor="confirmPassword" className="block text-gray-700">Re-enter Password</label>
                  <input
                      type="password"
                      id="confirmPassword"
                      className="w-full px-4 py-2 border rounded-lg"
                      placeholder="비밀번호를 다시 입력하세요."
                      value={confirmPassword}
                      onChange={(e) => handleConfirmPasswordChange(e.target.value)}
                  />
                  {passwordMatch !== null && (
                      <p className={`text-sm ${passwordMatch ? "text-green-600" : "text-red-600"}`}>
                        {passwordMatch ? "비밀번호가 일치합니다." : "비밀번호가 일치하지 않습니다."}
                      </p>
                  )}
                </div>
                <div>
                  <label htmlFor="nickname" className="block text-gray-700">Nickname</label>
                  <input
                      type="text"
                      id="nickname"
                      className="w-full px-4 py-2 border rounded-lg"
                      placeholder="닉네임을 입력하세요."
                      value={nickname}
                      onChange={(e) => setNickname(e.target.value)}
                  />
                </div>
                <button
                    type="button"
                    onClick={handleSignUp}
                    className="mt-4 w-full bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700"
                >
                  회원가입
                </button>
              </>
          )}
        </div>
      </div>
  );
};

export default SignUp;