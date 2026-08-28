export function SectionHeader({
  label,
  title,
  sub,
}: {
  label: string
  title: string
  sub?: string
}) {
  return (
    <div className="mb-10">
      <span className="text-xs font-semibold text-[#F5A623] uppercase tracking-widest">
        {label}
      </span>
      <h2 className="font-[Barlow_Condensed] text-4xl md:text-5xl font-bold text-white mt-1">
        {title}
      </h2>
      {sub && <p className="text-[#7a7d90] mt-2 max-w-xl text-sm leading-relaxed">{sub}</p>}
    </div>
  )
}
