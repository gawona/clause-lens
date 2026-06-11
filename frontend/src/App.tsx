import { BrowserRouter, Link, Route, Routes } from "react-router-dom";
import DocumentDetailPage from "./pages/DocumentDetailPage";
import DocumentListPage from "./pages/DocumentListPage";

const App = () => {
  return (
    <BrowserRouter>
      <div className="min-h-screen bg-slate-50 text-slate-900">
        <header className="border-b border-slate-200 bg-white">
          <div className="mx-auto flex max-w-7xl items-center justify-between px-6 py-4">
            <Link to="/" className="flex items-center gap-3 no-underline">
              <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-slate-900 text-sm font-bold text-white">
                CL
              </div>

              <div>
                <p className="text-lg font-bold leading-none text-slate-900">
                  ClauseLens
                </p>
                <p className="mt-1 text-xs text-slate-500">
                  AI Document Analysis Platform
                </p>
              </div>
            </Link>

            <nav>
              <Link
                to="/"
                className="rounded-lg px-3 py-2 text-sm font-medium text-slate-600 no-underline hover:bg-slate-100 hover:text-slate-900"
              >
                문서 목록
              </Link>
            </nav>
          </div>
        </header>

        <Routes>
          <Route path="/" element={<DocumentListPage />} />
          <Route path="/documents/:documentId" element={<DocumentDetailPage />} />
        </Routes>
      </div>
    </BrowserRouter>
  );
};

export default App;