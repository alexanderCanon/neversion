export function PlatformBadge({
  letters,
  color,
  size = 56,
}: {
  letters: string
  color: string
  size?: number
}) {
  const small = letters.length > 2
  return (
    <div
      className="flex items-center justify-center rounded-xl font-bold shrink-0 select-none shadow-md"
      style={{
        width: size,
        height: size,
        backgroundColor: color,
        color: '#fff',
        fontSize: small ? size * 0.22 : size * 0.33,
        letterSpacing: '-0.02em',
      }}
    >
      {letters}
    </div>
  )
}
