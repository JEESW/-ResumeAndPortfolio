import React from "react";
import Header from "./components/Header/Header";
import Footer from "./components/Footer/Footer";

function App() {
  return (
      <div>
        <Header />
        <main className="mt-8">
          <section id="home" className="h-screen flex items-center justify-center bg-gray-100">
            <h1 className="text-4xl font-bold">Welcome to Home Page</h1>
          </section>
          <section id="resume" className="h-screen flex items-center justify-center bg-gray-200">
            <h1 className="text-4xl font-bold">Resume Page</h1>
          </section>
          <section id="portfolio" className="h-screen flex items-center justify-center bg-gray-300">
            <h1 className="text-4xl font-bold">Portfolio Page</h1>
          </section>
        </main>
        <Footer />
      </div>
  );
}

export default App;