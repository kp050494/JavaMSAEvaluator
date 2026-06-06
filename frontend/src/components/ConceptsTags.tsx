export default function ConceptsTags({ concepts }: { concepts: string[] }) {
  return (
    <div className="flex flex-wrap gap-1.5">
      {concepts.map((c) => (
        <span
          key={c}
          className="rounded-md border border-accent-purple/30 bg-accent-purple/10 px-2 py-0.5 text-[11px] text-accent-purple"
        >
          {c}
        </span>
      ))}
    </div>
  );
}
