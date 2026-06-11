import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";

interface MarkdownViewerProps {
  content: string;
}

const MarkdownViewer = ({ content }: MarkdownViewerProps) => {
  return (
    <div className="space-y-4 text-sm leading-7 text-slate-700">
      <ReactMarkdown
        remarkPlugins={[remarkGfm]}
        components={{
          h1: ({ children }) => (
            <h1 className="mt-6 border-b border-slate-200 pb-2 text-xl font-bold text-slate-950">
              {children}
            </h1>
          ),
          h2: ({ children }) => (
            <h2 className="mt-5 border-b border-slate-200 pb-2 text-lg font-bold text-slate-900">
              {children}
            </h2>
          ),
          h3: ({ children }) => (
            <h3 className="mt-4 text-base font-bold text-slate-900">
              {children}
            </h3>
          ),
          p: ({ children }) => (
            <p className="whitespace-pre-line text-sm leading-7 text-slate-700">
              {children}
            </p>
          ),
          strong: ({ children }) => (
            <strong className="font-bold text-slate-950">{children}</strong>
          ),
          ul: ({ children }) => (
            <ul className="ml-5 list-disc space-y-1">{children}</ul>
          ),
          ol: ({ children }) => (
            <ol className="ml-5 list-decimal space-y-1">{children}</ol>
          ),
          li: ({ children }) => (
            <li className="pl-1 leading-7 text-slate-700">{children}</li>
          ),
          hr: () => <hr className="my-4 border-slate-200" />,
          blockquote: ({ children }) => (
            <blockquote className="rounded-xl border-l-4 border-slate-300 bg-slate-50 px-4 py-3 text-slate-600">
              {children}
            </blockquote>
          ),
          code: ({ children }) => (
            <code className="rounded bg-slate-100 px-1.5 py-0.5 text-xs font-semibold text-slate-800">
              {children}
            </code>
          ),
        }}
      >
        {content}
      </ReactMarkdown>
    </div>
  );
};

export default MarkdownViewer;